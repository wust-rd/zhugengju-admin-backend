# 12 - onemap_04-fill 市政工程安全一张图填报系统（建表与接口）

> 原型：`市政工程安全一张图填报系统_v2.html`（3812 行，列表 + 9 段详情 + 勾选式评分 + 新增项目向导）
> 后端：`modules/onemap_04-fill`（包 `com.jeesite.modules.onemap04fill`）
> 前端：`digital-zhugeng-frontend/packages/onemap_04-fill`，路由 `/onemap_04-fill`
> 分支：两端均为 `onemap_04-fill`

## 一、分期范围

一期（核心闭环）：项目台账列表/筛选/统计、9 段详情展示与编辑留痕、勾选式评分（后端计分）、危大/基坑登记、隐患登记、操作日志、新增项目。
外部系统数据（实名制/视频/扬尘噪声/塔机/无人机）**全部留空占位**，不写种子数据，不接真实接口。

## 二、表结构（达梦 DM8，schema=DIGITAL_ZHUGENG_DEV）

建表脚本：`docs/sql/OM4_TABLES.sql`（8 张表，业务表不带 `js_` 前缀）。

| 表 | 说明 |
|---|---|
| `OM4_PROJECT` | 项目台账主表（含分值、风险等级、评分勾选 JSON、完整度） |
| `OM4_PARTY` | 参建五方责任主体 |
| `OM4_APPROVAL` | 审批与手续共享（9 类） |
| `OM4_HAZARD` | 危大 / 超危大工程（除基坑） |
| `OM4_PIT` | 基坑工程 |
| `OM4_RISK` | 日常安全隐患 |
| `OM4_FILE` | 附件（落盘 + 元数据） |
| `OM4_LOG` | 操作日志 |

注意：`OM4_PROJECT.STATUS` 是 `DataEntity.status` 保留列；施工状态用 `BUILD_STATUS`。

## 三、接口（`/api/safetyfill/**`，免登录）

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `meta` | 字典 + 评分口径（类别树、扣分项、阈值、必传附件） |
| GET | `project/list` | 列表（district/cat/buildStatus/riskLevel/kw/page/pageSize） |
| GET | `project/get` | 详情（base/parties/approvals/hazards/pits/risks/files/score/review） |
| GET | `project/stats` | 台账统计（数量、危大、红色、完整度、区域/类别分布） |
| POST | `project/create` | 新增项目（手工建档，预置 9 类审批事项） |
| POST | `project/save` | 保存基本信息 + 五方 + 审批（记变更留痕） |
| POST | `hazard/save` / `hazard/delete` | 危大工程新增编辑 / 删除 |
| POST | `pit/save` | 基坑清单全量保存 |
| POST | `risk/save` / `risk/edit` / `risk/close` | 隐患登记 / 整改 / 闭环 |
| GET | `score/get` | 评分页数据（review + score） |
| POST | `score/save` | 保存评分（后端重新计分并回写） |
| POST | `score/reset` | 按台账重算勾选（不落库） |
| GET | `log/list` | 操作日志 |
| POST | `file/upload` / `file/download` / `file/delete` | 附件 |

框架约定：返回 `{"result":"true","message":"...","data":...}`；`Map` 型响应已手动包一层 `data`（`renderResult` 对 Map 会展开）。

## 四、评分口径（后端 `OmScoreEngine`，与原型 scoreFromSel 一致）

总分 100 = 工程类别 20 + 危大/超危大 35 + 基坑工程 20 + 日常隐患排查 25，分值越高越安全。

- 工程类别：按 `cat` 取静态分（隧道/轨道交通/综合管廊 5，桥梁 10，道排/房建配套 15）。
- 危大/超危大：基础档 非危大 35 / 仅危大 30 / 含超危大 25；扣分项 15（方案/论证附件缺失自动判定）、5（旁站缺失）、10（未验收即进入下道工序）；得分下限 0。
- 基坑：基础档 均不存在 20 / 仅危大 18 / 含超危大 16；扣分项 12（必传附件缺失自动判定）、8、4；下限 0。
- 隐患排查：`安全检查分数 × 0.25` 四舍五入，0~25。
- 等级：≤40 一级（红）/ ≤60 二级（橙）/ ≤80 三级（黄）/ 其余 四级（蓝）。

评分勾选结果存 `OM4_PROJECT.REVIEW_JSON`（`src`：empty/auto/manual），结果存 `SCORE_TOTAL` + `RISK_LEVEL`；未提交评分时列表按台账自动推算展示。

## 五、已知约定

- 附件落盘目录：`om4.upload-dir`，默认 `${user.home}/jeesite-om4-upload`。
- 操作人由前端传 `operator`，默认「张工」。
- 前端 `views/helpers/score.ts` 有一份与后端同规则的本地实现，用于评分页实时预览；保存以后端返回为准。

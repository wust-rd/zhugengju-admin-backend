package com.jeesite.modules.onemap04fill.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 市政安全一张图填报系统 - 字典与评分口径常量
 * <p>
 * 评分标准取自《市政工程安全风险分级评分标准》，与产品原型 v2 完全一致：
 * 工程类别 20 + 危大/超危大 35 + 基坑工程 20 + 日常隐患排查 25 = 100。
 *
 * @author wuchangyue
 * @version 2026-11-15
 */
public final class OmMeta {

	private OmMeta() {
	}

	/** 工程类别 */
	public static final List<String> CATS = List.of("轨道交通", "桥梁", "隧道", "道排", "综合管廊", "房建配套");

	/** 所属区域 */
	public static final List<String> DISTRICTS = List.of("江岸区", "江汉区", "硚口区", "汉阳区", "武昌区", "洪山区", "青山区");

	/** 施工状态 */
	public static final List<String> BUILD_STATUS = List.of("在建", "停工", "未开工", "竣工待验");

	/** 规模单位 */
	public static final List<String> SCALE_UNITS = List.of("万平方米", "公里", "延米");

	/** 危大工程实施阶段 */
	public static final List<String> STAGES = List.of("尚未开始", "实施中", "停工待复工", "完工");

	/** 专项方案状态 */
	public static final List<String> PLAN_STATUS = List.of("未编制", "已编制", "已审核", "已审批", "已交底");

	/** 专家论证结论 */
	public static final List<String> EXPERT = List.of("未论证", "通过", "修改后通过", "不通过");

	/** 基坑施工阶段 */
	public static final List<String> PIT_STAGES = List.of("土方开挖", "主体结构施工/拆换撑", "封顶及回填");

	/** 隐患类型 */
	public static final List<String> RISK_TYPES = List.of("临边洞口无防护", "未戴安全帽", "未穿反光衣", "未挂安全带",
			"起重设备限位失效", "临时用电线路破损", "脚手架连墙件缺失", "消防器材缺失", "裸土未覆盖", "材料堆放超限");

	/** 隐患等级 */
	public static final List<String> RISK_LEVELS = List.of("一般", "重大");

	/** 隐患状态 */
	public static final List<String> RISK_STATUS = List.of("待整改", "整改中", "已整改", "逾期未整改");

	/** 风险等级名称 */
	public static final Map<Integer, String> RISK_NAME = Map.of(1, "一级风险", 2, "二级风险", 3, "三级风险", 4, "四级风险");

	/** 工程类别静态分（分值越高越安全，固有风险高的类别给分低） */
	public static final Map<String, Integer> CAT_SCORE = Map.of(
			"隧道", 5, "轨道交通", 5, "综合管廊", 5,
			"桥梁", 10, "道排", 15, "房建配套", 15);

	/** 工程类别分值说明 */
	public static final Map<Integer, String> CAT_NOTE = Map.of(
			5, "隧道、盾构、顶管、综合管廊（固有风险最高）",
			10, "桥梁、高架立交、深隧配套工程",
			15, "道路、管网、给排水、机电等常规市政工程",
			20, "维修改造、小型零星项目");

	/** 工程类别分值文案 */
	public static final Map<Integer, String> CAT_TEXT = Map.of(
			5, "隧道、盾构、顶管、综合管廊",
			10, "桥梁、高架立交、深隧配套工程",
			15, "道路、管网、给排水、机电等常规市政工程",
			20, "维修改造、小型零星项目等");

	/** 危大/超危大扣分项 */
	public static final List<Map<String, Object>> HAZ_DED_OPTS = List.of(
			opts(15, "专项施工方案未上传或未履行编审，超危大未完成专家论证",
					"危大工程每个小项须上传专项施工方案；超危大工程每个小项还须上传专家论证意见；缺任一项即按本项扣分"),
			opts(5, "未落实现场旁站和过程管控，或现场管控与审批方案不一致", "无旁站记录、监管人员空缺，或实施与审批方案不符"),
			opts(10, "未组织分部分项工程验收即进入下一道工序", "已进入实施或完工，但无验收记录"));

	/** 基坑工程基础档位 */
	public static final List<Map<String, Object>> PIT_BASE_OPTS = List.of(
			opts(16, "存在超危大基坑（开挖深度 ≥5m）", "与危大基坑同时存在时，按本档计 16 分"),
			opts(18, "仅存在危大基坑（开挖深度 3~5m）", "不存在超危大基坑时 18 分"),
			opts(20, "危大、超危大基坑均不存在", "基础档 20 分（满分）"));

	/** 基坑工程扣分项 */
	public static final List<Map<String, Object>> PIT_DED_OPTS = List.of(
			opts(12, "未按规定编审专项方案、超危大未组织专家论证、未进行开挖条件验收、未监测或监测数据不实",
					"符合任一项即扣 12 分"),
			opts(8, "监测数据超限未处置、监测频次不满足方案要求、违规超挖、支护支撑滞后、基坑出现明显渗漏",
					"符合任一项即扣 8 分"),
			opts(4, "监测预警信息处置不及时、闭环滞后", "符合任一项即扣 4 分"));

	/** 危大工程（附件 1）细分类别 */
	private static final List<Map<String, Object>> HAZ_TREE = new ArrayList<>();

	/** 超危大工程（附件 2）细分类别 */
	private static final List<Map<String, Object>> SUP_TREE = new ArrayList<>();

	static {
		HAZ_TREE.add(tree("模板工程及支撑体系", "滑模", "爬模", "飞模", "隧道模", "混凝土模板支撑工程", "承重支撑体系"));
		HAZ_TREE.add(tree("起重吊装及起重设备",
				"采用非常规起重设备、方法，且单件起吊重量在 10kN 及以上的起重吊装工程",
				"采用起重机械进行安装的工程", "起重机械安装和拆卸工程"));
		HAZ_TREE.add(tree("脚手架工程",
				"搭设高度 24m 及以上的落地式钢管脚手架工程（包括采光井、电梯井脚手架）",
				"附着式升降脚手架工程", "悬挑式脚手架工程", "高处作业吊篮",
				"卸料平台、操作平台工程", "异型脚手架工程"));
		HAZ_TREE.add(tree("拆除工程",
				"可能影响行人、交通、电力设施、通讯设施或其他建（构）筑物安全的拆除工程",
				"文物保护建筑、优秀历史建筑或历史文化风貌区控制范围内的拆除工程"));
		HAZ_TREE.add(tree("暗挖工程",
				"矿山法施工的隧道、洞室工程", "盾构法施工的隧道、洞室工程", "顶管法施工的隧道、洞室工程"));
		HAZ_TREE.add(tree("其它",
				"建筑幕墙安装工程", "钢结构、网架和索膜结构安装工程", "人工挖孔桩工程", "水下作业工程",
				"装配式建筑混凝土预制构件安装工程",
				"采用新技术、新工艺、新材料、新设备可能影响工程施工安全，尚无国家、行业及地方技术标准的分部分项工程"));

		SUP_TREE.add(tree("模板工程及支撑体系",
				"滑模", "爬模", "飞模", "隧道模",
				"混凝土模板支撑工程：搭设高度 8m 及以上；或搭设跨度 18m 及以上，或施工总荷载（设计值）15kN/m² 及以上，或集中线荷载（设计值）20kN/m 及以上",
				"承重支撑体系：用于钢结构安装等满堂支撑体系，承受单点集中荷载 7kN 及以上"));
		SUP_TREE.add(tree("起重吊装及起重机械安装拆卸工程",
				"采用非常规起重设备、方法，且单件起吊重量在 100kN 及以上的起重吊装工程",
				"起重量 300kN 及以上，或搭设总高度 200m 及以上，或搭设基础标高在 200m 及以上的起重机械安装和拆卸工程"));
		SUP_TREE.add(tree("脚手架工程",
				"搭设高度 50m 及以上的落地式钢管脚手架工程",
				"提升高度在 150m 及以上的附着式升降脚手架工程或附着式升降操作平台工程",
				"分段架体搭设高度 20m 及以上的悬挑式脚手架工程"));
		SUP_TREE.add(tree("拆除工程",
				"码头、桥梁、高架、烟囱、水塔，或拆除中容易引起有毒有害气（液）体或粉尘扩散、易燃易爆事故发生的特殊建、构筑物的拆除工程",
				"文物保护建筑、优秀历史建筑或历史文化风貌区影响范围内的拆除工程"));
		SUP_TREE.add(tree("暗挖工程", "采用矿山法、盾构法、顶管法施工的隧道、洞室工程"));
		SUP_TREE.add(tree("其它",
				"施工高度 50m 及以上的建筑幕墙安装工程",
				"跨度 36m 及以上的钢结构安装工程，或跨度 60m 及以上的网架和索膜结构安装工程",
				"开挖深度 16m 及以上的人工挖孔桩工程",
				"水下作业工程",
				"重量 1000kN 及以上的大型结构整体顶升、平移、转体等施工工艺",
				"采用新技术、新工艺、新材料、新设备可能影响工程施工安全，尚无国家、行业及地方技术标准的分部分项工程"));
	}

	private static Map<String, Object> opts(int v, String t, String d) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("v", v);
		m.put("t", t);
		m.put("d", d);
		return m;
	}

	private static Map<String, Object> tree(String n, String... subs) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("n", n);
		m.put("subs", List.of(subs));
		return m;
	}

	public static List<Map<String, Object>> hazTree() {
		return HAZ_TREE;
	}

	public static List<Map<String, Object>> supTree() {
		return SUP_TREE;
	}

	/** 附件必传要求：危大只需专项方案，超危大需专项方案 + 专家论证 */
	public static final Map<String, List<String>> ATT_REQ = Map.of(
			"nod", List.of("plan"), "sup", List.of("plan", "argue"));

	public static final Map<String, String> ATT_LABEL = Map.of(
			"plan", "专项施工方案", "argue", "专家论证意见", "mont", "基坑监测数据");

	/** 风险评分维度口径说明 */
	public static final List<Map<String, Object>> DIMS = List.of(
			dim("工程类别", 20, "静态分，按工程类别取最高风险档"),
			dim("危大 / 超危工程（除基坑）", 35, "级别定基础档，扣分项与附件齐备情况自动计算"),
			dim("基坑工程", 20, "按深度定基础档，扣分项与附件齐备情况自动计算"),
			dim("日常隐患排查", 25, "安监智慧监管系统安全检查分数 × 0.25"));

	private static Map<String, Object> dim(String name, int full, String desc) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("name", name);
		m.put("full", full);
		m.put("desc", desc);
		return m;
	}

	/** 评分页与详情页共用的字典集合 */
	public static Map<String, Object> meta() {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("cats", CATS);
		m.put("districts", DISTRICTS);
		m.put("buildStatus", BUILD_STATUS);
		m.put("scaleUnits", SCALE_UNITS);
		m.put("stages", STAGES);
		m.put("planStatus", PLAN_STATUS);
		m.put("expert", EXPERT);
		m.put("pitStages", PIT_STAGES);
		m.put("riskTypes", RISK_TYPES);
		m.put("riskLevels", RISK_LEVELS);
		m.put("riskStatus", RISK_STATUS);
		m.put("riskName", RISK_NAME);
		m.put("catScore", CAT_SCORE);
		m.put("catNote", CAT_NOTE);
		m.put("catText", CAT_TEXT);
		m.put("catOpts", List.of(5, 10, 15, 20));
		m.put("hazDedOpts", HAZ_DED_OPTS);
		m.put("pitBaseOpts", PIT_BASE_OPTS);
		m.put("pitDedOpts", PIT_DED_OPTS);
		m.put("hazTree", HAZ_TREE);
		m.put("supTree", SUP_TREE);
		m.put("attReq", ATT_REQ);
		m.put("attLabel", ATT_LABEL);
		m.put("dims", DIMS);
		return m;
	}
}

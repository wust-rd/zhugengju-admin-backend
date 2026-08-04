# JeeSite 项目知识库（docs）

本目录是对 `/Users/ryan/project/jeesite` 项目（JeeSite 快速开发平台，V5 Spring Boot 4 版）的系统性学习笔记，按主题分类整理，可直接作为 **AI 提示词 / skill 知识库** 使用。

## 使用方式

- **作为人查阅**：按需打开对应主题文档。
- **作为 AI 提示词**：提问前，把与问题相关的文档内容（或整个文件路径）作为上下文喂给 AI，AI 即可基于项目真实结构回答，避免凭空猜测。
- **示例提问**：
  - "参考 `docs/07` 的代码模式，帮我给 xxx 模块新增一个 xxx 功能"
  - "我要在 Ubuntu 上部署 web-api，用 `docs/06` 的步骤，数据库是 PostgreSQL"
  - "`docs/04` 里 jdbc 配置怎么改？"

## 文档索引

| 文档 | 内容 | 适用场景 |
|---|---|---|
| [01-项目概览与技术栈](01-项目概览与技术栈.md) | 项目定位、版本、技术栈、环境要求 | 首次了解项目、环境搭建前 |
| [02-模块结构说明](02-模块结构说明.md) | 根目录结构、各 Maven 模块用途、依赖关系 | 搞清楚"哪个代码在哪个模块" |
| [03-核心框架与架构](03-核心框架与架构.md) | common 工具库、core 模块、分层架构、权限体系 | 理解框架设计、找代码位置 |
| [04-配置体系详解](04-配置体系详解.md) | 配置文件位置、各配置节说明、多环境配置 | 改端口/数据库/缓存等配置 |
| [05-数据库支持与初始化](05-数据库支持与初始化.md) | 支持的数据库、建库脚本、初始化流程 | 数据库选型、首次初始化 |
| [06-部署指南](06-部署指南.md) | 传统部署、Docker 部署、systemd 自启 | 部署到服务器 |
| [07-开发模式与代码规范](07-开发模式与代码规范.md) | Entity/Dao/Service/Controller 开发模式、编码规范 | 新功能开发、AI 生成代码 |
| [08-web-api专项详解](08-web-api专项详解.md) | web-api 模块定位、配置、接口路由、认证、打包部署 | 部署/开发 web-api 纯接口服务 |

## 项目一句话速查

- **项目名**：JeeSite 快速开发平台 V5（Spring Boot 4 版）
- **版本**：`5.18.1.springboot4-SNAPSHOT`
- **官网**：https://jeesite.com | **源码**：https://gitee.com/thinkgem/jeesite5 （分支 `v5.springboot4`）
- **技术栈**：Spring Boot 4 + MyBatis + Apache Shiro + J2Cache + Maven
- **环境要求**：JDK 17+（推荐 21）、Maven 3.8+
- **默认端口**：8980；web/web-api/web-vue 的 context-path 为 `/js`，web-fast 为根路径
- **数据库**：MySQL / PostgreSQL / Oracle / SQLServer / 达梦 / 人大金仓 / H2
- **本地运行**（web-fast）：`web-fast/bin/run-tomcat.sh`，账号 system/admin
- **默认登录账号**：管理员 `system`，密码 `admin`

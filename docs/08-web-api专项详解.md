# web-api 模块专项详解

> 本文档针对 `/web-api` 模块（`jeesite-web-api`），讲解它的定位、配置、接口路由、认证机制、打包部署等，是部署和开发分离端接口服务的权威参考。

## 1. 模块定位

`web-api` 是 JeeSite 的**纯接口服务模块**，专为**前后端分离**场景提供 HTTP API（给 Vue 前端、App 移动端调用），不包含后台管理界面。

核心特征一句话：**"关闭内置登录页和主框架页视图"**，只输出 JSON 接口。

### 1.1 与其它 web 模块的区别

| 模块 | 打包 | 端口 | context-path | 用途 |
|---|---|---|---|---|
| `web` | war | 8980 | `/js` | 一体化部署（后台管理 + 接口） |
| `web-api` | war | 8980 | `/js` | **纯接口**（前后端分离，`apiMode=true`） |
| `web-vue` | jar | 8980 | `/js` | 对接 Vue3 前端（`apiMode=true`） |
| `web-fast` | war | 8080 | 根路径 | 快速开发演示 |
| `web-mini` | - | - | - | 微信小程序相关 |
| `web-ai` | - | - | - | AI 功能 |

`web-api` 与 `web` 的代码几乎完全一致（同一个 core 模块），关键差异只在入口类和 `application.yml` 中的一个开关：

```yaml
# web-api 的 application.yml 中（核心开关）
apiMode: true
```

### 1.2 apiMode=true 的效果

- **登录页不渲染视图**：`LoginController` 中判断 `Global.isApiMode()` 为 true 时，直接返回提示 JSON（"Please visit front-end address."），不再返回登录 HTML 页面。
- **登录成功后返回 JSON**：分离端前端（Vue）拿到登录信息后自行管理登录态。
- **关闭内置后台框架页**：不提供主框架页面（`/js/a/...` 等管理页面）。

## 2. 项目结构与入口

### 2.1 目录结构

```
web-api/
├── pom.xml                  # Maven 配置（artifactId: jeesite-web-api）
├── README.md                # 官方说明
├── bin/
│   ├── package.sh           # 打包脚本（生成 web.war）
│   ├── run-tomcat.sh        # 内嵌 Tomcat 本地运行
│   ├── run-web.sh           # 直接 java -jar 运行
│   ├── init-data.sh         # 初始化数据库（建表 + 初始数据）
│   ├── docker-build.sh      # 构建 Docker 镜像
│   └── docker/
│       └── Dockerfile       # Dockerfile（基础镜像见 5.3）
└── src/
    ├── main/
    │   ├── java/com/jeesite/modules/ApiApplication.java   # 唯一入口类
    │   └── resources/
    │       ├── config/
    │       │   ├── application.yml        # 主配置（apiMode=true）
    │       │   ├── application-prod.yml   # 生产配置（MySQL 示例）
    │       │   ├── application-sharding.yml  # 分库分表配置
    │       │   ├── logback-spring.xml     # 日志配置
    │       │   └── ...
    │       └── static/                    # 静态资源（默认只有 favicon）
    └── test/                              # 测试（含初始化数据类）
```

### 2.2 入口类

```java
// web-api/src/main/java/com/jeesite/modules/ApiApplication.java
package com.jeesite.modules;

@SpringBootApplication
public class ApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }
}
```

入口类与 `web` 模块共用 `@SpringBootApplication` 自动扫描 `com.jeesite` 下所有组件，因此 `modules/core`、`modules/app`、`modules/cms` 等模块的 Controller 会全部被扫描进来——**web-api 本身不写业务代码，只负责把各模块的接口"聚合"成一个可部署服务**。

## 3. Maven 依赖与模块关系

`web-api/pom.xml` 关键点：

- **packaging**：`war`（打包后是 `web.war`，内置 Tomcat，可 `java -jar` 直接运行）
- **finalName**：`web`（输出文件名 `web.war`）
- **依赖的核心模块**：
  - `jeesite-module-core`：系统管理核心（用户/角色/菜单/权限等）
  - `jeesite-module-cms`：内容管理（新闻公告等）
  - `jeesite-module-app`：移动端接口（评论、升级检测等）
  - `jeesite-module-dbm`：数据库管理
  - `jeesite-module-test`：测试/示例
  - `jeesite-module-swagger`：在线接口文档
  - `jeesite-module-filemanager`、`jeesite-module-filepreview`：**默认被注释掉**（标准版不含，需要时取消注释）

> 意味着：web-api 部署后默认会包含 CMS 新闻、App 评论、Swagger 文档等接口，全部通过 `http://IP:8980/js/...` 暴露。

## 4. 配置详解

### 4.1 端口与路径（application.yml 关键项）

```yaml
server:
  port: 8980                 # 端口
  servlet:
    context-path: /js        # 上下文路径，所有接口都在 /js 下

spring:
  application:
    name: jeesite-web-api

# 核心开关
apiMode: true

# 管理路径前缀（继承自 core 模块默认值 /a，也可在此覆盖）
#adminPath: /a
```

**接口完整地址规则**：`http://IP:端口/上下文路径/管理前缀/控制器映射`

- 上下文路径：`/js`
- 管理前缀：`/a`（`adminPath`，默认值来自 `modules/core/src/main/resources/config/jeesite-core.yml`）
- 示例：登录接口 = `POST http://IP:8980/js/a/login`

### 4.2 数据库配置（默认 PostgreSQL）

```yaml
jdbc:
  type: postgresql
  driver: org.postgresql.Driver
  url: jdbc:postgresql://127.0.0.1:5432/jeesite_v5
  username: root
  password: 123456
  testSql: SELECT 1
```

> ⚠️ 重要：`application-prod.yml` 里是 **MySQL** 配置。如果强制用 PostgreSQL，**不要加 `--spring.profiles.active=prod`**，直接用默认配置启动即可（详见 `docs/06-部署指南.md` 的 PostgreSQL 方案）。

### 4.3 其它配置要点

- **登录认证**：Shiro 配置中 `loginUrl: ${adminPath}/login`（即 `/a/login`），支持 `validCode` 验证码参数、`__login`（GET 方式登录）等。
- **在线接口文档**：通过 `jeesite-module-swagger`（SpringDoc）提供，配置中 `springdoc` 的 `api-docs` 与 `swagger-ui` 均为 enabled。
- **日志**：`logback-spring.xml`，默认输出到 `logs/` 目录。
- **静态资源**：`static/` 目录默认只有 favicon，接口服务不提供页面资源。

## 5. 打包与部署

### 5.1 打包

```bash
cd web-api/bin
./package.sh
```

- 脚本内部会执行 `mvn package`，产物在 `web-api/maven/` 目录下，文件名 **`web.war`**。
- `web.war` 是**可执行 Spring Boot 包**（内置 Tomcat），无需单独安装 Tomcat。

### 5.2 启动（传统方式）

```bash
mkdir -p ~/logs
cd web-api/maven
nohup java -jar web.war > ~/logs/web-api.log 2>&1 &
# 日志出现 "Started ApiApplication" 即成功
curl http://127.0.0.1:8980/js/a/login
```

### 5.3 Docker 部署

`bin/docker-build.sh` + `bin/docker/Dockerfile`：

- 基础镜像：OpenJDK（JRE 运行环境）
- 启动方式：`java -jar web.war`
- 构建镜像命令示例（在 `web-api/bin` 目录）：
  ```bash
  ./docker-build.sh
  ```

### 5.4 初始化数据库

```bash
cd web-api/bin
echo | ./init-data.sh
```

- 自动创建数据表并写入初始数据（管理员 `system/admin` 等）。
- 首次部署只执行一次；`init-data.sh` 内部有 `read` 等待回车，用 `echo |` 可免交互。
- 数据库初始化前必须先建好数据库（如 PostgreSQL 的 `jeesite_v5`，见 `docs/05` 和 `docs/06`）。

### 5.5 本机快速启动（开发时）

```bash
cd web-api/bin
./run-tomcat.sh      # 或 ./run-web.sh
```

> 本机需要：JDK 17+、Maven 3.8+，且 PostgreSQL 已建好 `root/123456/jeesite_v5`。

## 6. 接口路由与认证

### 6.1 路由前缀一览

| 前缀 | 含义 | 是否需登录 |
|---|---|---|
| `/js/a/**` | 后台管理接口（adminPath） | 需要（Shiro 认证） |
| `/js/app/**` | App 移动端公开接口（如升级检测、意见反馈） | 多数不需要 |
| `/js/f/**` | 前台（front）接口 | 按配置 |
| `/js/swagger-ui/**`、`/js/v3/api-docs` | 在线接口文档 | 配置决定 |

### 6.2 登录接口示例

**登录（表单提交）**：

```http
POST http://IP:8980/js/a/login
Content-Type: application/x-www-form-urlencoded

username=system&password=admin
```

成功返回 JSON（含用户信息、认证信息），后续请求携带 Shiro 会话（Cookie 或 Token）。

**登出**：

```http
GET http://IP:8980/js/a/logout
```

### 6.3 权限注解

接口控制器使用 Shiro 注解控制权限，例如：

```java
@RequiresPermissions("sys:user:edit")
@PostMapping("/save")
public Result save(User user) {
    return Result.success(...);
}
```

需要权限的接口请对照 `sys_menu` 表中的权限标识配置角色权限。

## 7. App 移动端接口（modules/app）

`web-api` 会扫描 `modules/app` 的 Controller，提供移动端接口：

| 接口路径 | 说明 |
|---|---|
| `/app/upgrade/check` | App 版本升级检测（公开） |
| `/app/comment/save` | 意见反馈/评论提交 |
| `/a/app/appUpgrade/**` | 升级管理后台接口 |
| `/a/app/appComment/**` | 评论管理后台接口 |

完整路径示例：`http://IP:8980/js/app/upgrade/check`

## 8. 在线接口文档（Swagger）

- 依赖 `jeesite-module-swagger`（标准版模块，打包时已包含）。
- 基于 SpringDoc，接口文档地址（在启用 swagger 的配置下）：
  - Swagger UI：`http://IP:8980/js/swagger-ui/index.html`
  - OpenAPI JSON：`http://IP:8980/js/v3/api-docs`
- Controller 中使用 `@Tag` / `@Operation` 注解描述接口（如 `AppPublicServiceController`），即可在文档中展示。

## 9. 常用操作速查

| 需求 | 命令/位置 |
|---|---|
| 打包 | `web-api/bin/package.sh` → `web-api/maven/web.war` |
| 本地运行 | `web-api/bin/run-tomcat.sh` 或 `run-web.sh` |
| 初始化数据库 | `web-api/bin/init-data.sh` |
| 改端口 | `web-api/src/main/resources/config/application.yml` → `server.port` |
| 改数据库 | 同上 → `jdbc:` 段 |
| 查看日志 | `tail -f ~/logs/web-api.log`（或 `web-api` 下 `logs/`） |
| 验证服务 | `curl http://127.0.0.1:8980/js/a/login` |
| 新增接口 | 在任意模块（如 `modules/app`）的 `web/` 包下加 Controller |

## 10. 常见问题

**Q1：登录接口返回 "Please visit front-end address."？**
这是 apiMode=true 的预期行为，web-api 不渲染登录页，前端应使用 JSON 接口自行实现登录页。

**Q2：`/js/a/**` 接口 401/登录跳转？**
确认请求已登录（带 Session/Token）；或该接口标注了 `@RequiresPermissions` 但当前用户无权限。

**Q3：启动报数据库连接失败？**
检查 PostgreSQL 是否已启动、`jeesite_v5` 库和 `root/123456` 账号是否已建（见 `docs/06` 第 2 步）。

**Q4：想开放 Swagger 给前端看？**
确认 `jeesite-module-swagger` 未注释、且配置中 springdoc enabled；生产环境建议按需关闭。

**Q5：部署后 8980 端口不通？**
检查防火墙：`sudo ufw allow 8980/tcp`；确认服务确实已启动（看日志）。

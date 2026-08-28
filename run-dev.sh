#!/bin/sh
# ============================================================
# web-api 前台调试模式启动脚本 (mvn spring-boot:run)
#
# 数据库  : PostgreSQL  jeesite_v5 (root/123456)
# 端口    : 8980        context-path: /js
# 运行方式: 前台运行，日志实时输出到当前终端
# 停止    : Ctrl + C
# 首次    : 会编译并 install 上游依赖(common/core/cms/app/test)，之后很快
#
# 注意    : 不要加 --spring.profiles.active=prod (prod 是 MySQL 配置)
# ============================================================
set -e

# 脚本位于项目根目录
ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT_DIR"

# JeeSite 惯例：若设置过 JAVA_HOME17 则优先（Spring Boot 4 需 JDK 17+，
# 本机默认即 JDK 21，不设置也正常）
if [ -n "$JAVA_HOME17" ] && [ -d "$JAVA_HOME17" ]; then
  export JAVA_HOME="$JAVA_HOME17" PATH="$JAVA_HOME17/bin:$PATH"
fi

echo "=========================================================="
echo " 项目根: $ROOT_DIR"
mvn -v
echo ""
echo " 前台调试模式启动 web-api ..."
echo " 端口: 8980    数据库: PostgreSQL jeesite_v5    context-path: /js"
echo ""
echo " [步骤1/2] 编译并安装上游依赖到本地仓库 ..."
echo "=========================================================="

# 第一步：把 web-api 的上游依赖装进本地 m2（多模块下 spring-boot:run 需要它们）
mvn -pl web-api -am install -Dmaven.test.skip=true -DskipTests

echo ""
echo "=========================================================="
echo " [步骤2/2] 在前台启动 web-api (Spring Boot) ..."
echo " 停止: Ctrl + C"
echo "=========================================================="
echo ""

# 第二步：仅在 web-api 模块上运行（不加 -am，避免 goal 作用到 parent 等无主类模块）
mvn -pl web-api spring-boot:run

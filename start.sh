#!/bin/sh
# web-api 启动/停止脚本
# 用法: ./start.sh {start|stop|restart|status|log}

JAVA=/usr/java/jdk-21/bin/java
JAR_FILE=web.war
# 堆大小按目标机内存调整
JAVA_OPTS="-Xms4096m -Xmx4096m"
# 端口在 ./config/application.yml 的 server.port 配置

cd "$(dirname "$0")" || exit 1
mkdir -p logs
PID_FILE=backend.pid
LOG_FILE=logs/backend.log
KEEP_LOGS=5

# 校验 PID 对应进程的命令行含 "-jar web.war", 防止系统复用 PID 后误判/误杀
is_app_pid() {
    [ -n "$1" ] && ps -p "$1" -o args= 2>/dev/null | grep -q -- "-jar $JAR_FILE"
}

get_pid() {
    if [ -f "$PID_FILE" ]; then
        pid=$(cat "$PID_FILE" 2>/dev/null)
        if is_app_pid "$pid"; then
            echo "$pid"; return
        fi
        rm -f "$PID_FILE"
    fi
    # PID 文件缺失时按命令行兜底查找, 找回后回写, 避免再拉起第二个实例
    pid=$(pgrep -f "java.*-jar $JAR_FILE" 2>/dev/null | head -n 1)
    if is_app_pid "$pid"; then
        echo "$pid" > "$PID_FILE"
        echo "$pid"
    fi
}

# 启动前轮转上次日志, 只保留最近 KEEP_LOGS 份
rotate_log() {
    if [ -f "$LOG_FILE" ]; then
        mv "$LOG_FILE" "logs/backend-$(date +%Y%m%d%H%M%S).log"
    fi
    ls -1t logs/backend-*.log 2>/dev/null | tail -n +$((KEEP_LOGS + 1)) |
        while IFS= read -r f; do rm -f "$f"; done
}

start() {
    pid=$(get_pid)
    if [ -n "$pid" ]; then
        echo "[backend] 已在运行, PID=$pid"; return 0
    fi
    rotate_log
    nohup "$JAVA" $JAVA_OPTS -jar "$JAR_FILE" --spring.config.additional-location=file:./config/ > "$LOG_FILE" 2>&1 &
    pid=$!
    echo "$pid" > "$PID_FILE"
    echo "[backend] 启动中, PID=$pid, 日志: $LOG_FILE"
    # 观察启动结果: 进程退出即报错并打印日志尾部; 出现 Spring Boot 的 Started 行视为就绪
    i=0
    while [ $i -lt 60 ]; do
        if ! is_app_pid "$pid"; then
            echo "[backend] 启动失败, 进程已退出, 日志尾部 30 行:"
            tail -n 30 "$LOG_FILE"
            rm -f "$PID_FILE"
            return 1
        fi
        if grep -q "Started .* in .* seconds (JVM running for" "$LOG_FILE" 2>/dev/null; then
            echo "[backend] 启动成功, PID=$pid"
            return 0
        fi
        sleep 1; i=$((i+1))
    done
    echo "[backend] 60 秒内未确认启动完成, 进程仍在运行, 请查看日志: tail -f $LOG_FILE"
    return 0
}

stop() {
    pid=$(get_pid)
    if [ -z "$pid" ]; then
        echo "[backend] 未在运行"; return 0
    fi
    echo "[backend] 停止中, PID=$pid ..."
    if ! kill "$pid" 2>/dev/null; then
        if is_app_pid "$pid"; then
            echo "[backend] 无权限停止 PID $pid, 请用启动时的账号操作"; return 1
        fi
        rm -f "$PID_FILE"
        echo "[backend] 进程已退出"; return 0
    fi
    i=0
    while [ $i -lt 30 ] && kill -0 "$pid" 2>/dev/null; do
        sleep 1; i=$((i+1))
    done
    if kill -0 "$pid" 2>/dev/null; then
        echo "[backend] 优雅停机超时(30 秒), 强制结束"
        kill -9 "$pid" 2>/dev/null
        j=0
        while [ $j -lt 5 ] && kill -0 "$pid" 2>/dev/null; do
            sleep 1; j=$((j+1))
        done
        if kill -0 "$pid" 2>/dev/null; then
            echo "[backend] 强制结束后进程仍在, PID=$pid, 请手动排查"; return 1
        fi
    fi
    rm -f "$PID_FILE"
    echo "[backend] 已停止"
    return 0
}

case "$1" in
    start)   start ;;
    stop)    stop ;;
    restart) stop || exit 1; sleep 2; start ;;
    status)  pid=$(get_pid)
             if [ -n "$pid" ]; then
                 echo "[backend] 运行中, PID=$pid"
             else
                 echo "[backend] 未运行"
             fi ;;
    log)     [ -f "$LOG_FILE" ] || { echo "[backend] 暂无日志 $LOG_FILE"; exit 1; }
             tail -f "$LOG_FILE" ;;
    *)       echo "用法: $0 {start|stop|restart|status|log}"; exit 1 ;;
esac

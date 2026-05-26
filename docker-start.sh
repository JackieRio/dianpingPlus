#!/bin/bash

# 黑马点评 Plus Docker 启动脚本

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查 Docker 是否安装
check_docker() {
    if ! command -v docker &> /dev/null; then
        print_error "Docker 未安装，请先安装 Docker"
        exit 1
    fi

    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        print_error "Docker Compose 未安装，请先安装 Docker Compose"
        exit 1
    fi
}

# 启动所有服务
start_all() {
    print_info "启动所有服务..."
    docker compose up -d
    print_info "所有服务已启动"
    print_info "访问地址: http://localhost:8080"
    print_info "后端 API: http://localhost:8081"
}

# 仅启动基础设施服务（不启动后端应用）
start_infra() {
    print_info "启动基础设施服务（MySQL、Redis、Kafka）..."
    docker compose up -d mysql redis kafka
    print_info "基础设施服务已启动"
    print_info "后端应用请在 IDE 中启动 HmDianPingApplication"
}

# 停止所有服务
stop_all() {
    print_info "停止所有服务..."
    docker compose down
    print_info "所有服务已停止"
}

# 重启所有服务
restart_all() {
    print_info "重启所有服务..."
    docker compose restart
    print_info "所有服务已重启"
}

# 查看服务状态
status() {
    print_info "服务状态:"
    docker compose ps
}

# 查看日志
logs() {
    if [ -n "$1" ]; then
        docker compose logs -f "$1"
    else
        docker compose logs -f
    fi
}

# 清理数据卷（谨慎使用）
clean() {
    print_warn "即将删除所有数据卷，这将清除所有数据！"
    read -p "确认删除？(y/N): " confirm
    if [ "$confirm" = "y" ] || [ "$confirm" = "Y" ]; then
        docker compose down -v
        print_info "数据卷已删除"
    else
        print_info "已取消"
    fi
}

# 构建镜像
build() {
    print_info "构建后端应用镜像..."
    docker compose build backend
    print_info "镜像构建完成"
}

# 显示帮助
show_help() {
    echo "黑马点评 Plus Docker 管理脚本"
    echo ""
    echo "用法: $0 [命令]"
    echo ""
    echo "命令:"
    echo "  start       启动所有服务（包括后端应用）"
    echo "  start-infra 仅启动基础设施服务（MySQL、Redis、Kafka）"
    echo "  stop        停止所有服务"
    echo "  restart     重启所有服务"
    echo "  status      查看服务状态"
    echo "  logs        查看所有服务日志"
    echo "  logs [服务]  查看指定服务日志（如：logs mysql）"
    echo "  build       构建后端应用镜像"
    echo "  clean       清理所有数据卷（会删除所有数据）"
    echo "  help        显示此帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 start-infra  # 启动基础设施，在 IDE 中启动后端"
    echo "  $0 start        # 启动所有服务（包含后端）"
    echo "  $0 logs mysql   # 查看 MySQL 日志"
}

# 主函数
main() {
    check_docker

    case "${1:-help}" in
        start)
            start_all
            ;;
        start-infra)
            start_infra
            ;;
        stop)
            stop_all
            ;;
        restart)
            restart_all
            ;;
        status)
            status
            ;;
        logs)
            logs "$2"
            ;;
        build)
            build
            ;;
        clean)
            clean
            ;;
        help|*)
            show_help
            ;;
    esac
}

main "$@"

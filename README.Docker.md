# Docker 部署指南

## 前置条件

- 安装 [Docker](https://docs.docker.com/get-docker/)
- 安装 [Docker Compose](https://docs.docker.com/compose/install/)

## 快速开始

### 方式一：仅启动基础设施（推荐开发时使用）

这种方式只启动 MySQL、Redis、Kafka，后端应用在 IDE 中启动，方便调试。

```bash
# 启动基础设施
./docker-start.sh start-infra

# 或使用 docker compose
docker compose up -d mysql redis kafka
```

然后在 IntelliJ IDEA 中运行 `HmDianPingApplication` 主类。

### 方式二：启动所有服务

```bash
# 启动所有服务（包括后端应用）
./docker-start.sh start

# 或使用 docker compose
docker compose up -d
```

访问地址：
- 前端：http://localhost:8080（需要配置 Nginx 和前端文件）
- 后端 API：http://localhost:8081

## 常用命令

### 使用管理脚本

```bash
# 查看帮助
./docker-start.sh help

# 启动所有服务
./docker-start.sh start

# 仅启动基础设施
./docker-start.sh start-infra

# 停止所有服务
./docker-start.sh stop

# 重启所有服务
./docker-start.sh restart

# 查看服务状态
./docker-start.sh status

# 查看日志
./docker-start.sh logs           # 所有服务
./docker-start.sh logs mysql     # 仅 MySQL
./docker-start.sh logs redis     # 仅 Redis
./docker-start.sh logs kafka     # 仅 Kafka

# 构建后端镜像
./docker-start.sh build

# 清理数据卷（会删除所有数据）
./docker-start.sh clean
```

### 使用 Docker Compose

```bash
# 启动所有服务
docker compose up -d

# 停止所有服务
docker compose down

# 查看服务状态
docker compose ps

# 查看日志
docker compose logs -f
docker compose logs -f mysql

# 重启服务
docker compose restart

# 重新构建并启动
docker compose up -d --build
```

## 服务说明

| 服务 | 端口 | 说明 |
|------|------|------|
| mysql | 3306 | MySQL 5.7 数据库 |
| redis | 6379 | Redis 6 缓存 |
| zookeeper | 2181 | Zookeeper（Kafka 依赖） |
| kafka | 9092 | Kafka 消息队列 |
| backend | 8081 | 后端应用（可选） |

## 配置说明

### 环境变量

可以在 `.env` 文件中修改配置：

```bash
# MySQL 配置
MYSQL_ROOT_PASSWORD=157359

# Redis 配置（留空表示无密码）
REDIS_PASSWORD=

# 后端应用配置
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/db_hmdp?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=Asia/Shanghai
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=157359
SPRING_REDIS_HOST=redis
SPRING_REDIS_PORT=6379
SPRING_REDIS_PASSWORD=
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
```

### 数据持久化

数据存储在 Docker 数据卷中：
- `mysql-data`：MySQL 数据
- `redis-data`：Redis 数据
- `zookeeper-data`：Zookeeper 数据
- `kafka-data`：Kafka 数据

清理数据卷：
```bash
./docker-start.sh clean
```

## 常见问题

### 1. 端口被占用

如果端口被占用，可以修改 `docker-compose.yml` 中的端口映射：

```yaml
ports:
  - "3307:3306"  # 将 MySQL 映射到 3307 端口
```

### 2. MySQL 初始化失败

检查 MySQL 日志：
```bash
docker compose logs mysql
```

### 3. Kafka 无法连接

确保 Zookeeper 已启动：
```bash
docker compose ps
docker compose logs zookeeper
```

### 4. 后端应用启动失败

检查后端日志：
```bash
docker compose logs backend
```

常见原因：
- 数据库连接失败：检查 MySQL 是否启动
- Redis 连接失败：检查 Redis 是否启动
- Kafka 连接失败：检查 Kafka 是否启动

## 开发建议

1. **开发时**：使用 `start-infra` 仅启动基础设施，在 IDE 中启动后端应用
2. **测试时**：使用 `start` 启动所有服务，进行端到端测试
3. **生产部署**：根据实际环境修改 `.env` 配置

## 架构图

```
┌─────────────────────────────────────────────────────────────┐
│                      Docker Network                         │
│                                                             │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │  MySQL   │  │  Redis   │  │  Kafka   │  │ Backend  │   │
│  │  :3306   │  │  :6379   │  │  :9092   │  │  :8081   │   │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘   │
│       │             │             │             │           │
│       └─────────────┴─────────────┴─────────────┘           │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

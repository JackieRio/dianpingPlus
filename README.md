<div align="center">

# 🔥 黑马点评 Plus

### *一个让你面试 Offer 拿到手软的项目*

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.3.12-green?style=flat-square&logo=spring-boot)](https://spring.io/projects/spring-boot)
[![MyBatis Plus](https://img.shields.io/badge/MyBatis%20Plus-3.4.3-blue?style=flat-square)](https://baomidou.com/)
[![Redis](https://img.shields.io/badge/Redis-6.x-red?style=flat-square&logo=redis)](https://redis.io/)
[![Kafka](https://img.shields.io/badge/Kafka-2.x-orange?style=flat-square&logo=apache-kafka)](https://kafka.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue?style=flat-square&logo=docker)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)](LICENSE)

**基于黑马点评拓展开发，引入 Kafka 消息队列 + AOP 多维度限流**

*让高并发、缓存、分布式不再是面试噩梦* 😎

</div>

---

## 🎯 这个项目能让你学到什么？

```
┌─────────────────────────────────────────────────────────────────┐
│  🚀 高并发秒杀        │  💾 多级缓存策略      │  🔐 分布式锁     │
│  Lua 原子操作         │  穿透/击穿/雪崩       │  Redisson 实战   │
├─────────────────────────────────────────────────────────────────┤
│  📨 消息队列          │  🎯 滑动窗口限流      │  📍 地理位置     │
│  Kafka 异步解耦       │  AOP + Lua 脚本       │  Redis GEO      │
├─────────────────────────────────────────────────────────────────┤
│  🐳 容器化部署        │  🔄 Feed 流设计       │  📝 签到系统     │
│  Docker 一键启动      │  推拉结合模式         │  Bitmap 实现     │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🛠️ 技术栈

| 分类 | 技术 | 版本 | 一句话点评 |
|------|------|------|------------|
| 🏗️ 框架 | Spring Boot | 2.3.12 | *你永远可以相信 Spring* |
| 📦 ORM | MyBatis-Plus | 3.4.3 | *CRUD ？不存在的* |
| 💾 缓存 | Redis + Lettuce | 6.x | *快就完事了* |
| 🔒 分布式锁 | Redisson | 3.13.6 | *锁王就是我* |
| 📨 消息队列 | Kafka | 2.x | *异步才是王道* |
| 🗄️ 数据库 | MySQL | 5.x | *老牌选手* |
| 🧰 工具库 | Hutool + Lombok | 5.7.17 | *少写代码多摸鱼* |
| 🎯 AOP | Spring AOP | - | *切面编程 YYDS* |
| 📜 脚本 | Lua | - | *原子操作の神* |
| 🐳 容器 | Docker | - | *一键启动真香* |

---

## 🚀 快速启动

### 方式一：Docker 一键启动（推荐）

```bash
# 克隆项目
git clone https://github.com/your-username/hm-dianping-plus.git
cd hm-dianping-plus

# 一键启动所有服务
./docker-start.sh start

# 访问 http://localhost:8080
```

就这么简单，**三个字：快、准、狠！** 🎉

### 方式二：本地开发模式

```bash
# 1. 启动基础设施（MySQL、Redis、Kafka）
./docker-start.sh start-infra

# 2. 在 IDEA 中运行 HmDianPingApplication

# 3. 开始你的表演 🎭
```

### 方式三：纯手工打造

```bash
# 1. 启动 MySQL，导入数据库
mysql -u root -p < src/main/resources/db/hmdp.sql

# 2. 启动 Redis
redis-server

# 3. 启动 Kafka（需要先启动 Zookeeper）
zookeeper-server-start config/zookeeper.properties
kafka-server-start config/server.properties

# 4. 修改 application.yaml 中的配置

# 5. 运行 HmDianPingApplication
```

*手工党表示：仪式感拉满！* 🧙‍♂️

---

## 🏗️ 项目架构

```
hm-dianping-plus/
├── 🎯 src/main/java/com/hmdp/
│   ├── 📁 config/              # 配置中心（Redis、Kafka、跨域、拦截器）
│   ├── 🎮 controller/          # REST 接口（给前端爸爸用的）
│   ├── ⚙️ service/             # 业务逻辑（核心战场）
│   │   └── impl/               # 实现类（别问，问就是面向接口编程）
│   ├── 🗄️ mapper/              # 数据访问（SQL 都在这）
│   ├── 📦 entity/              # 实体类（数据库表的镜像）
│   ├── 📨 dto/                 # 传输对象（接口的快递盒）
│   └── 🔧 utils/               # 工具类（瑞士军刀们）
│       ├── CacheClient.java    # 缓存三剑客（穿透/击穿/雪崩）
│       ├── RedisIdWorker.java  # 分布式 ID 生成器（雪花算法）
│       ├── interceptor/        # 拦截器双雄（登录校验 + Token 刷新）
│       ├── listener/           # Kafka 消费者（秒杀订单、缓存更新）
│       └── ratelimiter/        # 限流组件（滑动窗口 + AOP）
├── 📜 src/main/resources/
│   ├── seckill_advance.lua     # 秒杀 Lua 脚本（原子操作の王）
│   ├── limiter.lua             # 限流 Lua 脚本（滑动窗口）
│   └── mapper/                 # MyBatis XML
├── 🐳 docker-compose.yml       # Docker 编排（一键启动）
├── 🐳 Dockerfile               # 后端镜像构建
└── 📝 README.md                # 就是你正在看的这个
```

---

## 💡 核心功能解析

### 🔥 秒杀系统（面试必问！）

```
用户请求 → Lua 原子校验 → Kafka 异步下单 → 数据库落库
    │           │              │              │
    │     ┌────┴────┐    ┌────┴────┐    ┌────┴────┐
    │     │ 时间窗口 │    │ 异步解耦 │    │ 分布式锁 │
    │     │ 库存校验 │    │ 削峰填谷 │    │ 防重复   │
    │     │ 一人一单 │    │ 消息持久 │    │ Redisson │
    │     └─────────┘    └─────────┘    └─────────┘
    │
    └──→ 返回订单 ID（乐观响应）
```

**亮点**：
- ✅ Lua 脚本保证原子性，不会超卖
- ✅ Kafka 异步处理，高并发不卡顿
- ✅ Redisson 分布式锁，防重复消费
- ✅ 限流注解保护，防止被刷爆

### 💾 多级缓存策略

```
┌─────────────────────────────────────────────────────────┐
│                     缓存三剑客                           │
├─────────────────┬─────────────────┬─────────────────────┤
│   缓存穿透      │    缓存击穿      │    缓存雪崩         │
│   (查不到)      │    (并发重建)    │    (同时过期)       │
├─────────────────┼─────────────────┼─────────────────────┤
│   空值缓存      │   互斥锁        │    随机 TTL         │
│   布隆过滤器    │   逻辑过期        │    多级缓存         │
└─────────────────┴─────────────────┴─────────────────────┘
```

### 🎯 滑动窗口限流

```java
// 一个注解搞定限流，就这么优雅
@RateLimiter(
    window = 10,      // 10 秒窗口
    limit = 10,       // 最多 10 次请求
    type = RateLimitType.USER,  // 按用户限流
    message = "慢点慢点，服务器顶不住了！"
)
@PostMapping("/api/xxx")
public Result doSomething() {
    // 你的业务逻辑
}
```

### 📍 附近店铺搜索

```bash
# Redis GEO 实现，5km 内的奶茶店，按距离排序
GEOSEARCH shop:geo:1 FROMLONLAT 116.397128 39.916527 BYRADIUS 5 km
```

*再也不用担心找不到附近的奶茶店了！* 🧋

---

## 📡 API 接口

### 用户模块 👤

| 方法 | 路径 | 说明 | 需登录 |
|------|------|------|--------|
| POST | `/user/code` | 发送验证码 | ❌ |
| POST | `/user/login` | 用户登录 | ❌ |
| POST | `/user/logout` | 用户登出 | ✅ |
| GET | `/user/me` | 获取当前用户 | ✅ |
| POST | `/user/sign` | 每日签到 | ✅ |

### 店铺模块 🏪

| 方法 | 路径 | 说明 | 需登录 |
|------|------|------|--------|
| GET | `/shop/{id}` | 查询店铺 | ❌ |
| GET | `/shop/of/type` | 按类型查询（支持 GEO） | ❌ |
| POST | `/shop` | 新增店铺 | ✅ |
| PUT | `/shop` | 更新店铺 | ✅ |

### 秒杀模块 ⚡

| 方法 | 路径 | 说明 | 需登录 |
|------|------|------|--------|
| POST | `/voucher/seckill` | 创建秒杀券 | ✅ |
| POST | `/voucher-order/seckill/{id}` | 秒杀下单 | ✅ |
| GET | `/voucher/list/{shopId}` | 查询优惠券 | ❌ |

### 博客模块 📝

| 方法 | 路径 | 说明 | 需登录 |
|------|------|------|--------|
| POST | `/blog` | 发布笔记 | ✅ |
| PUT | `/blog/like/{id}` | 点赞/取消点赞 | ✅ |
| GET | `/blog/hot` | 热门博客 | ❌ |
| GET | `/blog/of/follow` | 关注的人的博客 | ✅ |

### 社交模块 👥

| 方法 | 路径 | 说明 | 需登录 |
|------|------|------|--------|
| PUT | `/follow/{id}/{isFollow}` | 关注/取关 | ✅ |
| GET | `/follow/common/{id}` | 共同关注 | ✅ |

---

## 🐳 Docker 命令速查

```bash
# 🚀 启动
./docker-start.sh start          # 启动所有服务
./docker-start.sh start-infra    # 仅启动基础设施

# 🛑 停止
./docker-start.sh stop           # 停止所有服务

# 📊 查看
./docker-start.sh status         # 查看服务状态
./docker-start.sh logs           # 查看所有日志
./docker-start.sh logs mysql     # 查看 MySQL 日志

# 🔧 维护
./docker-start.sh build          # 构建后端镜像
./docker-start.sh restart        # 重启所有服务
./docker-start.sh clean          # 清理数据（慎用！）
```

---

## 🙏 致谢

- [黑马程序员](https://www.itheima.com/) - 原始项目提供者
- [Spring Boot](https://spring.io/projects/spring-boot) - 没有它就没有这个项目
- [Redis](https://redis.io/) - 快就完事了
- [Kafka](https://kafka.apache.org/) - 异步消息の神
- 所有贡献者 - *你们是最棒的！*

---

<div align="center">

### 如果这个项目对你有帮助，请给个 ⭐ Star 支持一下！

**你的 Star 是我更新的动力！** 🚀

[![Star History Chart](https://api.star-history.com/svg?repos=Dylan4real/hm-dianping-plus&type=Date)](https://star-history.com/#Dylan4real/hm-dianping-plus&Date)

---

*Made with ❤️ and ☕*

*如果代码有 Bug，那一定是特性！* 🐛➡️🦋

</div>

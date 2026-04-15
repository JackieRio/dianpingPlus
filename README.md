# 黑马点评 Plus

> 本项目基于黑马点评拓展开发，在原项目基础上引入Kafka，并通过AOP实现多维度限流，对标大众点评核心业务场景，重点实践高并发、缓存与分布式等技术点。

## 技术栈

| 分类 | 技术 |
|------|------|
| 框架 | Spring Boot 2.3、MyBatis-Plus 3.4 |
| 缓存 | Redis（Lettuce 连接池）、Redisson 分布式锁 |
| 消息队列 | Kafka |
| 数据库 | MySQL 5.x |
| 工具库 | Hutool、Lombok |
| 其他 | Spring AOP、Lua 脚本 |

## 项目结构

```
src/main/java/com/hmdp/
├── config/          # 配置类（Redis、Redisson、Kafka、MVC、跨域）
├── controller/      # REST 接口层
├── service/         # 业务逻辑层（impl/ 为实现）
├── mapper/          # MyBatis-Plus Mapper
├── entity/          # 数据库实体
├── dto/             # 传输对象（Result、LoginFormDTO 等）
└── utils/
    ├── CacheClient.java          # 缓存工具（穿透/互斥锁/逻辑过期）
    ├── CacheManager.java         # 缓存更新/清理（Kafka 联动）
    ├── RedisIdWorker.java        # Redis 全局唯一 ID 生成器
    ├── SimpleRedisLock.java      # 简单 Redis 分布式锁
    ├── interceptor/              # 登录校验 & Token 刷新拦截器
    ├── listener/                 # Kafka 消费者（秒杀订单、缓存更新/清理）
    └── ratelimiter/              # 滑动窗口限流（AOP + Lua 脚本）
```

## 核心功能

### 用户模块
- 手机号验证码登录，验证码存 Redis（TTL 2 分钟）
- Token 基于 UUID 存入 Redis Hash，双拦截器实现无感续期

### 店铺模块
- 多级缓存策略：`CacheClient` 封装**缓存穿透**（空值缓存）、**缓存击穿**（互斥锁 / 逻辑过期）三种方案
- 店铺信息按地理位置（GEO）支持附近搜索

### 秒杀优惠券
- Lua 脚本原子校验库存 + 时间窗口 + 一人一单，结果写入 Kafka
- Kafka 消费者异步落库，Redisson 分布式锁防重复消费
- 订单创建后异步更新 Redis 缓存，失败降级为缓存清理

### 博客 & 社交
- 点赞排行：Redis ZSet 记录点赞时间，取 Top5
- 关注 Feed 流：发布笔记推送至粉丝收件箱（ZSet），滚动分页拉取

### 限流
- `@RateLimiter` 注解 + AOP + Redis 滑动窗口 Lua 脚本，支持 IP / 用户 / 方法三种粒度

## 快速启动

1. 启动 MySQL（导入 `src/main/resources/db/hmdp.sql`）
2. 启动 Redis（默认 `127.0.0.1:6379`）
3. 启动 Kafka（默认 `127.0.0.1:9092`）
4. 修改 `application.yaml` 中数据库密码等配置
5. 运行 `HmDianPingApplication`，服务监听 **8081** 端口

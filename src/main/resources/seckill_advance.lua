-- 异步秒杀Lua脚本
-- 1.参数列表
-- 1.1.优惠券id
local voucherId = ARGV[1]
-- 1.2.用户id
local userId = ARGV[2]
-- 1.3.订单id
local orderId = ARGV[3]

-- 2.数据key
-- 2.1.库存key
local stockKey = 'seckill:stock:' .. voucherId
-- 2.2.订单key
local orderKey = 'seckill:order:' .. voucherId

-- 3.获取库存信息
local stockInfoJson = redis.call('get', stockKey)
if not stockInfoJson then
    -- 库存信息不存在
    return 3
end

-- 4.解析库存信息
local cjson = require "cjson"
local stockInfo = cjson.decode(stockInfoJson)

-- 5.检查秒杀时间
-- 获取当前时间（秒级时间戳）
local currentTime = tonumber(redis.call('time')[1])
-- 将ISO 8601格式时间转换为时间戳
local function parseDateTime(dateStr)
    -- 处理 "2024-01-01T10:00:00" 格式
    local year, month, day, hour, min, sec = dateStr:match("(%d+)-(%d+)-(%d+)T(%d+):(%d+):(%d+)")
    if year then
        -- 使用os.time进行转换
        return os.time({year=year, month=month, day=day, hour=hour, min=min, sec=sec})
    end
    -- 如果是数字时间戳，直接返回
    return tonumber(dateStr)
end

local beginTime = parseDateTime(stockInfo.beginTime)
local endTime = parseDateTime(stockInfo.endTime)

if currentTime < beginTime then
    -- 秒杀未开始
    return 4
end
if currentTime > endTime then
    -- 秒杀已结束
    return 5
end

-- 6.判断库存是否充足
if stockInfo.stock <= 0 then
    -- 库存不足
    return 1
end

-- 7.判断用户是否已下单
if redis.call('sismember', orderKey, userId) == 1 then
    -- 用户已下单
    return 2
end

-- 8.扣减库存
stockInfo.stock = stockInfo.stock - 1
redis.call('set', stockKey, cjson.encode(stockInfo))

-- 9.添加用户到已下单集合
redis.call('sadd', orderKey, userId)

-- 10.设置库存key的TTL作为兜底机制
redis.call('expire', stockKey, 1800)  -- 30分钟TTL
redis.call('expire', orderKey, 1800)  -- 30分钟TTL

-- 11.返回订单信息
return orderId
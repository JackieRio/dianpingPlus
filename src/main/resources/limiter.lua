local key = KEYS[1]
local window = tonumber(ARGV[1]) -- 时间窗口(单位为秒)
local limit = tonumber(ARGV[2]) -- 时间窗口内限制的次数
local now = tonumber(ARGV[3])  -- 当前时间(单位为毫秒)

-- 校验传进来的参数是否为空,若为空直接返回错误
if not window or not limit or not now then
  return redis.error_reply("Invalid input parameters")
end

window = window * 1000  -- 单位由秒->毫秒

-- 删除超出时间窗口的数据
-- Redis命令ZREMRANGEBYSCORE: 从有序集合中移除指定分数区间的成员，删除所有分数在 [0,now-window) 的请求记录
-- now是当前时间戳,window是时间窗口, 0是最小分数,now-window是时间窗口外最大的分数,这样就能删除超出窗口的元素
redis.call('ZREMRANGEBYSCORE', key, 0, now - window)

-- 获取当前窗口内的请求数量
-- Redis命令ZCARD: 返回有序集合中成员的数量,因为已经删除超出时间窗口的元素，所以直接返回集合的成员数量即可获取当前时间窗口的请求数
local current = redis.call('ZCARD', key)

-- 若"当前时间窗口内的请求数量" 小于 "时间窗口内限制的次数"
if current < limit then
  -- 添加当前请求（使用毫秒时间戳+随机数作为member,时间戳作为score）
  math.randomseed(now)
  local random = math.random(1000000)
  redis.call('ZADD', key, now, now .. '-' .. random)
  -- 更新过期时间
  redis.call('EXPIRE', key, window / 1000)
  return current + 1
else
  -- 若"当前时间窗口内的请求数量" 大于等于 "时间窗口内限制的次数", 则应该限制本次请求,返回 0,表示限流
  return 0
end

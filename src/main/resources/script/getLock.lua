if redis.call('set',KEYS[1],ARGV[1],'NX','PX',ARGV[2]) then
    return '1'
else
    return '0'
end

--[[
    参数解释：
    KEYS【1】：key值是为要加的锁定义的字符串常量

    ARGV【1】：value值是 request id, 用来防止解除了不该解除的锁. 可用 UUID

    ARGV【2】: 过期时间，锁占用时间一般不会太长，业务处理占用锁时间太长会造成其他线程阻塞太久。
--]]
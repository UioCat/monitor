package com.uio.monitor.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public final class CacheService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private DefaultRedisScript<String> getLockRedisScript;
    private DefaultRedisScript<String> releaseLockRedisScript;

    private StringRedisSerializer argsStringSerializer;
    private StringRedisSerializer resultStringSerializer;

    private final String EXEC_RESULT = "1";
    private static final String RELEASE_LOCK_SCRIPT = "script/releaseLock.lua";
    private static final String LOCK_SCRIPT = "script/getLock.lua";

    @PostConstruct
    public void init() {
        argsStringSerializer = new StringRedisSerializer();
        resultStringSerializer = new StringRedisSerializer();

        getLockRedisScript = new DefaultRedisScript<String>();
        getLockRedisScript.setResultType(String.class);
        releaseLockRedisScript = new DefaultRedisScript<String>();
        releaseLockRedisScript.setResultType(String.class);

        // 初始化装载 lua 脚本
        getLockRedisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource(LOCK_SCRIPT)));
        releaseLockRedisScript.setScriptSource(
            new ResourceScriptSource(new ClassPathResource(RELEASE_LOCK_SCRIPT)));

    }

    /**
     * 加锁操作
     *
     * @param key        Redis 锁的 key 值
     * @param requestId  请求id，防止解了不该由自己解的锁 (随机生成)
     * @param expireTime 锁的超时时间(毫秒)
     * @param retryTimes 重试次数
     * @return true or false
     */
    public boolean lock(String key, String requestId, String expireTime, int retryTimes) {
        if (retryTimes < 0) { retryTimes = 0; }

        try {
            int count = 0;
            while (true) {
                String result = stringRedisTemplate.execute(getLockRedisScript, argsStringSerializer,
                    resultStringSerializer,
                    Collections.singletonList(key), requestId, expireTime);
                log.debug("result:{}, type:{}", result, result.getClass().getName());
                if (EXEC_RESULT.equals(result)) {
                    return true;
                } else {
                    if (retryTimes == count) {
                        log.warn("has tried {} times, failed to acquire lock for key:{}, requestId:{}", count, key,
                            requestId);
                        return false;
                    }
                    count++;
                    log.warn("try to acquire lock {} times for key:{}, requestId:{}", count, key, requestId);
                    Thread.sleep(100);
                }
            }
        } catch (InterruptedException e) {
            log.error("execute lock error ", e);
        }
        return false;
    }

    /**
     * 解锁操作
     *
     * @param key       Redis 锁的 key 值
     * @param requestId 请求 id, 防止解了不该由自己解的锁 (随机生成)
     * @return true or false
     */
    public boolean unLock(String key, String requestId) {
        String result = stringRedisTemplate.execute(releaseLockRedisScript, argsStringSerializer,
            resultStringSerializer,
            Collections.singletonList(key), requestId);
        if (EXEC_RESULT.equals(result)) {
            return true;
        }
        return false;
    }

    /**
     * 根据key 获取过期时间
     *
     * @param key 键 不能为null
     * @return 时间(秒) 返回0代表为永久有效
     */
    public long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 判断key是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     */
    public boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除缓存
     *
     * @param key 可以传一个值 或多个
     */
    @SuppressWarnings("unchecked")
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 普通缓存获取
     *
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 普通缓存放入
     *
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */
    public boolean put(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 普通缓存放入并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    public boolean put(String key, Object value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                put(key, value);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 递增
     *
     * @param key   键
     * @param delta 要增加几(大于0)
     * @return
     */
    public long incr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 递减
     *
     * @param key   键
     * @param delta 要减少几(小于0)
     * @return
     */
    public long decr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, -delta);
    }
}
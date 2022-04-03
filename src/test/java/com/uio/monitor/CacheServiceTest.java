package com.uio.monitor;

import com.uio.monitor.common.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootTest
@Slf4j
class CacheServiceTest {

    @Autowired
    private CacheService cacheService;

    /**
     * 所有任务共享变量，要修改的值
     */
    private static int number = 0;

    @Test
    void putAndGetDataTest() {
        String key = "test_for_put_data";
        String value = "test_for_put_data_value";
        cacheService.put(key, value);
        String redisValue = cacheService.get(key).toString();
        log.info("cacheService.get:{}", redisValue);
        Assert.isTrue(redisValue.equals(value), "redis put,get value error");
    }


    @Test
    void lockAndReleaseLockTest() {
        String key = "prefix_key123456"; // redis 锁的 key 值
        String expireTime = "5000";// 锁的超时时间(毫秒)，评估任务时间，建议任务的时间不要太长
        int retryTimes = 3;// 获取锁的重试次数

        number = 0;// 共享变量
        int threadCount = 100;// 线程任务个数

        List<Thread> list = new ArrayList<Thread>();
        for (int i = 0; i < threadCount; i++) {

            list.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    // request id, 防止解了不该由自己解的锁。
                    String requestId = UUID.randomUUID().toString();
                    while (true) {
                        //这里循环操作，以确保该线程一定获得锁并执行线程任务
                        if (cacheService.lock(key, requestId, expireTime, retryTimes)) {
                            try {
                                // 调用业务逻辑
                                doSomething();
                            } finally {
                                cacheService.unLock(key, requestId);
                            }
                            break;
                        }
                    }
                }
            }));
        }

        /**
         * 启动所有任务线程
         */
        for (Thread t : list) {
            t.start();
        }

        //轮询状态，等待所有子线程完成
        while (true) {
            int aliveThreadCount = 0;
            for (Thread t : list) {
                if (t.isAlive()) {
                    ++aliveThreadCount;
                }
            }
            if (aliveThreadCount == 0) {
                log.debug("All Threads are completed!");
                break;
            } else {
                log.debug("Threads have not yet completed， sleep 5s!");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        //打印最终结果值： NUM
        log.info("Completed! The final value of NUM is : {}", number);
        Assert.isTrue(number == threadCount, "lock and releaseLock error");
    }

    /**
     * 模拟业务逻辑处理：NUM +1 操作
     */
    private void doSomething() {
        try {
            Thread.sleep(Double.valueOf(2000D * Math.random()).intValue());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int tmp = number;
        tmp = tmp + 1;
        number = tmp;
    }

}

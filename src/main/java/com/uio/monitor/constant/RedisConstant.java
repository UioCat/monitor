package com.uio.monitor.constant;

/**
 * @author han xun
 * Date 2021/11/21 18:49
 * Description: Redis常量
 */
public class RedisConstant {

    /**
     * 服务器监控分布式锁
     */
    private static final String SERVER_MONITOR_LOCK = "server_monitor_lock";

    /**
     * 自动打卡分布式锁
     */
    private static final String PUSH_CARD_LOCK = "push_card_lock";

    /**
     * 锁过期时间
     */
    public static final String EXPIRE_TIME = "30000";

    /**
     * 获取锁次数
     */
    public static final int RETRY_TIMES = 3;

    public static String getPushCardLock(String schoolNumber) {
        return schoolNumber + "_" + PUSH_CARD_LOCK;
    }

    public static String getServerMonitorLock(String ip) {
        return SERVER_MONITOR_LOCK + "_" + ip;
    }
}

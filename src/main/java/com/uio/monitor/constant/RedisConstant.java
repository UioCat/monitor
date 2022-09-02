package com.uio.monitor.constant;

/**
 * @author han xun
 * Date 2021/11/21 18:49
 * Description: Redis常量
 */
public class RedisConstant {

    /**
     * 服务器监控分布式锁前缀
     */
    private static final String SERVER_MONITOR_LOCK = "server_monitor_lock";
    /**
     * 自动打卡分布式锁前缀
     */
    private static final String PUSH_CARD_LOCK = "push_card_lock";
    /**
     * 定时消息分布式锁前缀
     */
    private static final String TIMING_MESSAGE_LOCK = "timing_message_lock";
    /**
     * 周期账单锁
     */
    private static final String PERIOD_BILL_LOCK = "SCAN_PERIOD_BILL_LOCK";
    /**
     * 锁过期时间
     */
    public static final String EXPIRE_TIME = "30000";

    /**
     * 获取锁次数
     */
    public static final int RETRY_TIMES = 3;

    /**
     * 获取打卡程序分布式锁名
     * @param schoolNumber
     * @return
     */
    public static String getPushCardLock(String schoolNumber) {
        return schoolNumber + "_" + PUSH_CARD_LOCK;
    }

    /**
     * 获取服务器拉起分布式锁名
     * @param ip
     * @return
     */
    public static String getServerMonitorLock(String ip) {
        return SERVER_MONITOR_LOCK + "_" + ip;
    }

    /**
     * 定期消息分布式锁名
     * @param id
     * @return
     */
    public static String getTimingMessageLock(String id) {
        return TIMING_MESSAGE_LOCK + "_" + id;
    }

    /**
     * 周期账单分布式锁
     */
    public static String getPeriodBillLock(String id) {
        return PERIOD_BILL_LOCK + "_" + id;
    }
}

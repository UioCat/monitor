package com.uio.monitor.constant;

/**
 * @author han xun
 * Date 2021/10/12 22:24
 * Description:
 */
public interface MonitorConstant {

    /**
     * 夜间时间
     */
    Integer NIGHT_HOUR = 20;

    /**
     * 健康监测定时任务cron表达式
     */
    String HEALTH_MONITOR_CRON = "0 0/1 * * * ?";

    /**
     * 本人邮箱
     */
    String MY_EMAIL = "406453373@qq.com";

    String SERVER_URL = "https://www.uiofield.top/server/monitor";
}

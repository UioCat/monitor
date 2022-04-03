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

}

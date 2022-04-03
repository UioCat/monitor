package com.uio.monitor.timer;

import com.alibaba.fastjson.JSON;
import com.uio.monitor.common.CacheService;
import com.uio.monitor.constant.MonitorConstant;
import com.uio.monitor.manager.ConfigManager;
import com.uio.monitor.vo.ServerMessage;
import com.uio.monitor.service.EmailService;
import com.uio.monitor.service.MonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author han xun
 * Date 2021/10/15 19:40
 * Description: 服务器健康检测定时器
 */
@Component
@EnableScheduling
@Slf4j
public class MonitorTimer {

    @Autowired
    private EmailService emailService;
    @Autowired
    private ConfigManager configManager;
    @Autowired
    private MonitorService monitorService;
    @Autowired
    private CacheService cacheService;

    private final static String ALARM_RECEIVE = "406453373@qq.com";
    private final static String ALARM_EMAIL_SUBJECT = "服务器异常提醒";

    /**
     * 定时监测
     */
    @Scheduled(cron = MonitorConstant.HEALTH_MONITOR_CRON)
    public void monitorTimer() {
        log.info("server health monitor timer launch");
        List<ServerMessage> list = configManager.getServerList();
        for (ServerMessage serverMessage : list) {
            try {
                //保持服务器运行
                monitorService.keepTomcatAlive(serverMessage);
                log.info("monitor serverMessage:{}", JSON.toJSONString(serverMessage));
            } catch (Exception e) {
                 // 发送邮件
                emailService.sendNonRepeatMessage(ALARM_RECEIVE,
                        ALARM_EMAIL_SUBJECT,
                        "服务器监控程序：" + serverMessage.getName() + "出现异常，请及时查看");
            }
        }
    }
}

package com.uio.monitor.timer;

import com.uio.monitor.common.CacheService;
import com.uio.monitor.constant.RedisConstant;
import com.uio.monitor.service.PushMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author han xun
 * Date 2022/3/28 23:42
 * Description: 推送消息扫描器
 */
@Component
@EnableScheduling
@Slf4j
public class PushMessageScanner {

    @Autowired
    private CacheService cacheService;
    @Autowired
    private Map<String, PushMessageService> pushMessageServiceMap;

    /**
     * 扫描消息
     */
    @Scheduled(cron = "1 * * * * ?")
    public void scannerMessage() {


    }
}

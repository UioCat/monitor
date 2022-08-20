package com.uio.monitor.timer;

import com.alibaba.fastjson.JSON;
import com.uio.monitor.common.*;
import com.uio.monitor.constant.RedisConstant;
import com.uio.monitor.entity.TimingMessageDO;
import com.uio.monitor.manager.TimingMessageManager;
import com.uio.monitor.service.TimingMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author han xun
 * Date 2022/3/28 23:42
 * Description: 推送消息扫描器
 */
@Component
@EnableScheduling
@Slf4j
public class TimingMessageScanner {

    @Autowired
    private CacheService cacheService;
    @Autowired
    private TimingMessageManager timingMessageManager;
    @Autowired
    private TimingMessageService timingMessageService;

    @Value("${spring.profiles.active}")
    private String env;

    private final static String ENV = "interior_prod";

    /**
     * 5分钟锁
     */
    private final static Long TIMING_MESSAGE_LOCK_TIME = 5 * 1000L;

    /**
     * 扫描定时消息
     * 10s扫描
     */
    @Scheduled(cron = "*/10 * * * * ?")
    public void scannerMessage() {
        if (!ENV.equals(env)) {
            return;
        }
        // 发送待发送的消息
        List<TimingMessageDO> timingMessageDOList = timingMessageManager.queryReadyMessage();
        Optional.ofNullable(timingMessageDOList).orElse(Collections.emptyList()).forEach(item -> {
            Long id = item.getId();
            String lockName = RedisConstant.getTimingMessageLock(id.toString());
            String uuid = UUID.randomUUID().toString();
            try {
                boolean lock = cacheService.lock(lockName, uuid, TIMING_MESSAGE_LOCK_TIME.toString(), 0);
                if (lock) {
                    log.info("get lock success, lockName:{}, lockTime:{}", lockName, TIMING_MESSAGE_LOCK_TIME);
                    String pushWay = item.getPushWay();
                    PushWayEnum pushWayEnum = PushWayEnum.getByName(pushWay);
                    if (pushWayEnum == null) {
                        log.warn("pushWayEnum is null, timingMessageDO:{}", JSON.toJSONString(item));
                        return;
                    }
                    timingMessageService.pushMessage(pushWayEnum, item);
                }
            } finally {
                cacheService.unLock(lockName, uuid);
            }
        });

        List<TimingMessageDO> timingMessageDOS = timingMessageManager.queryAbnormalMessage();
        if (!CollectionUtils.isEmpty(timingMessageDOS)) {
            List<Long> abnormalMessageIdList = timingMessageDOS.stream().map(TimingMessageDO::getId)
                    .collect(Collectors.toList());
            log.info("abnormalMessageIdList:{}", JSON.toJSONString(abnormalMessageIdList));
            timingMessageManager.updateMessageStateByOriginState(abnormalMessageIdList, PushStateEnum.INIT.name(),
                    PushStateEnum.PROCESSING.name());
        }

    }

}

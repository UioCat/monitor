package com.uio.monitor.timer;

import com.alibaba.fastjson.JSON;
import com.uio.monitor.common.*;
import com.uio.monitor.constant.RedisConstant;
import com.uio.monitor.entity.TimingMessageDO;
import com.uio.monitor.manager.TimingMessageManager;
import com.uio.monitor.service.PushMessageService;
import com.uio.monitor.service.TimingMessageService;
import com.uio.monitor.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

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

    /**
     * 5分钟锁
     */
    private final static Long TIMING_MESSAGE_LOCK_TIME = 5 * 1000L;
    /**
     * 扫描消息
     * 10s扫描
     */
    @Scheduled(cron = "*/10 * * * * ?")
    public void scannerMessage() {
        // 发送待发送的消息
        List<TimingMessageDO> timingMessageDOList = timingMessageManager.queryReadyMessage();
        Optional.ofNullable(timingMessageDOList).orElse(Collections.emptyList()).forEach(item -> {
            Long id = item.getId();
            String lockName = RedisConstant.getTimingMessageLock(id.toString());
            String uuid = UUID.randomUUID().toString();
            try {
                boolean lock = cacheService.lock(lockName, uuid, TIMING_MESSAGE_LOCK_TIME.toString(), 0);
                if (lock) {
                    String pushWay = item.getPushWay();
                    PushWayEnum pushWayEnum = PushWayEnum.getByName(pushWay);
                    if (pushWayEnum == null) {
                        log.warn("pushWayEnum is null, timingMessageDO:{}", JSON.toJSONString(item));
                        return;
                    }
                    timingMessageService.sendMessage(pushWayEnum, item);
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

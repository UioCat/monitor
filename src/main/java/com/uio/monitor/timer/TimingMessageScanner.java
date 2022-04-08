package com.uio.monitor.timer;

import com.alibaba.fastjson.JSON;
import com.uio.monitor.common.*;
import com.uio.monitor.constant.RedisConstant;
import com.uio.monitor.entity.TimingMessageDO;
import com.uio.monitor.manager.TimingMessageManager;
import com.uio.monitor.service.PushMessageService;
import com.uio.monitor.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

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
    private Map<String, PushMessageService> pushMessageServiceMap;
    @Autowired
    private TimingMessageManager timingMessageManager;

    private final static Long TIMING_MESSAGE_LOCK_TIME = 2 * 60 * 1000L;
    /**
     * 扫描消息
     * 10s扫描
     */
    @Scheduled(cron = "*/10 * * * * ?")
    public void scannerMessage() {
        List<TimingMessageDO> timingMessageDOList = timingMessageManager.queryReadyMessage();
        if (CollectionUtils.isEmpty(timingMessageDOList)) {
            log.warn("no timing message ready");
        }
        timingMessageDOList.forEach(item -> {
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
                        throw new CustomException(BackEnum.DATA_ERROR);
                    }
                    String serviceName = pushWayEnum.getServiceName();
                    PushMessageService pushMessageService = pushMessageServiceMap.get(serviceName);
                    if (pushMessageService == null) {
                        log.warn("cannot find pushMessageService, serviceName:{}, timingMessageDO:{}",
                                serviceName, JSON.toJSONString(item));
                        throw new CustomException(BackEnum.DATA_ERROR);
                    }
                    pushMessageService.sendMessage(item.getCreator(), item.getReceiver(), pushWayEnum, item.getMessage());
                    // 发送完成更新数据库
                    timingMessageManager.updateMessageState(item.getId(),
                            PushStateEnum.FINISH, PushStateEnum.INIT);
                    // 插入一条新的消息（如果需要）
                    this.insertNextMessageDO(item);
                }
            } finally {
                cacheService.unLock(lockName, uuid);
            }
        });
    }

    private void insertNextMessageDO(TimingMessageDO originTimingMessageDO) {
        String cycleUnit = originTimingMessageDO.getCycleUnit();
        Date pushDateTime = originTimingMessageDO.getPushDateTime();
        Integer pushCycleCount = originTimingMessageDO.getPushCycle();

        CycleUnitEnum cycleUnitEnum = CycleUnitEnum.getByName(cycleUnit);
        if (cycleUnitEnum == null) {
            log.warn("cycleUnitEnum is null, data error while insertNextMessageDO, originTimingMessageDO:{}",
                    JSON.toJSONString(originTimingMessageDO));
            throw new CustomException(BackEnum.DATA_ERROR);
        }
        if (cycleUnitEnum == CycleUnitEnum.NONE) {
            log.info("this timing message without cycle, ");
            return;
        }
        Date newDate = null;
        switch (cycleUnitEnum) {
            case MINUTE: {
                newDate = DateUtils.getOffMinutes(pushDateTime, pushCycleCount);
                break;
            }
            case HOUR: {
                newDate = DateUtils.getOffHours(pushDateTime, pushCycleCount);
                break;
            }
            case DAY: {
                newDate = DateUtils.getOffDays(pushDateTime, pushCycleCount);
                break;
            }
            case WEEK: {
                newDate = DateUtils.getOffWeeks(pushDateTime, pushCycleCount);
                break;
            }
            case MONTH: {
                newDate = DateUtils.getOffMonths(pushDateTime, pushCycleCount);
                break;
            }
            case YEAR: {
                newDate = DateUtils.getOffYears(pushDateTime, pushCycleCount);
                break;
            }
        }
        TimingMessageDO timingMessageDO = new TimingMessageDO();
        timingMessageDO.setGmtCreate(new Date());
        timingMessageDO.setGmtModify(new Date());
        timingMessageDO.setCreator(originTimingMessageDO.getCreator());
        timingMessageDO.setModifier(originTimingMessageDO.getModifier());
        timingMessageDO.setDeleted(false);
        timingMessageDO.setPushDateTime(newDate);
        timingMessageDO.setState(PushStateEnum.INIT.name());
        timingMessageDO.setPushWay(originTimingMessageDO.getPushWay());
        timingMessageDO.setReceiver(originTimingMessageDO.getReceiver());
        timingMessageDO.setMessage(originTimingMessageDO.getMessage());
        timingMessageDO.setPushCycle(originTimingMessageDO.getPushCycle());
        timingMessageDO.setCycleUnit(originTimingMessageDO.getCycleUnit());
        timingMessageDO.setEffective(originTimingMessageDO.getEffective());
        timingMessageManager.insertMessage(timingMessageDO);
    }
}

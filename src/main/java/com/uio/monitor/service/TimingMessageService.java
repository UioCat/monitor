package com.uio.monitor.service;

import com.alibaba.fastjson.JSON;
import com.uio.monitor.common.*;
import com.uio.monitor.controller.req.AddTimingMessageReq;
import com.uio.monitor.controller.req.UpdateTimingMessageReq;
import com.uio.monitor.controller.resp.TimingMessageDTO;
import com.uio.monitor.entity.TimingMessageDO;
import com.uio.monitor.manager.TimingMessageManager;
import com.uio.monitor.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * @author han xun
 * Date 2022/4/4 23:46
 * Description: 定时消息服务
 */
@Service
@Slf4j
public class TimingMessageService {

    @Autowired
    private TimingMessageManager timingMessageManager;
    @Autowired
    private Map<String, PushMessageService> pushMessageServiceMap;

    /**
     * 加入一条消息
     * @param userId
     * @param addTimingMessageReq
     * @return
     */
    public Boolean addTimingMessage(Long userId, AddTimingMessageReq addTimingMessageReq) {
        return null;
    }

    public Boolean updateTimingMessage(Long userId, UpdateTimingMessageReq updateTimingMessageReq) {
        return null;
    }

    public List<TimingMessageDTO> getTimingMessageList(Long userId, Integer pageNum, Integer pageSize,
                                                       PushStateEnum pushStateEnum, PushWayEnum pushWay, Boolean effective) {
        return null;
    }

    /**
     * 推送一条消息
     * @param pushWayEnum
     * @param item
     * @return
     */
    @Transactional
    public Boolean sendMessage(PushWayEnum pushWayEnum, TimingMessageDO item) {
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
        return true;
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

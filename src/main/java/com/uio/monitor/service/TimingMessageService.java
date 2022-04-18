package com.uio.monitor.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.uio.monitor.common.*;
import com.uio.monitor.controller.req.AddTimingMessageReq;
import com.uio.monitor.controller.req.UpdateTimingMessageReq;
import com.uio.monitor.controller.resp.TimingMessageDTO;
import com.uio.monitor.entity.TimingMessageDO;
import com.uio.monitor.manager.TimingMessageManager;
import com.uio.monitor.mapper.TimingMessageDOMapper;
import com.uio.monitor.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author han xun, Drun1baby
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
     *
     * @param userId
     * @param addTimingMessageReq
     * @return
     */
    public Boolean addTimingMessage(Long userId, AddTimingMessageReq addTimingMessageReq) {
        TimingMessageDO timingMessageDO = new TimingMessageDO();
        timingMessageDO.setCreator(userId.toString());
        timingMessageDO.setModifier(userId.toString());
        timingMessageDO.setPushDateTime(addTimingMessageReq.getPushDateTime());
        timingMessageDO.setPushWay(addTimingMessageReq.getPushWay());
        timingMessageDO.setState(PushStateEnum.INIT.name());
        timingMessageDO.setReceiver(addTimingMessageReq.getReceiver());
        timingMessageDO.setMessage(addTimingMessageReq.getMessage());
        timingMessageDO.setPushCycle(addTimingMessageReq.getPushCycle());
        timingMessageDO.setCycleUnit(addTimingMessageReq.getCycleUnit());
        timingMessageDO.setPushWay(addTimingMessageReq.getPushWay());
        timingMessageDO.setEffective(addTimingMessageReq.getEffective());
        timingMessageManager.insertMessage(timingMessageDO);
        return true;
    }
    /**
     * 删除消息与更新消息
     * @param userId
     * @param updateTimingMessageReq
     * @return
     */
    public Boolean updateTimingMessage(Long userId, UpdateTimingMessageReq updateTimingMessageReq) {
        if (Boolean.TRUE.equals(updateTimingMessageReq.getDeleted())) {
            // 删除数据
            timingMessageManager.deleteById(userId);
        } else {
            // 更新数据
            TimingMessageDO timingMessageDOUpdate = new TimingMessageDO();
            timingMessageDOUpdate.setId(updateTimingMessageReq.getTimingMessageId());
            timingMessageDOUpdate.setModifier(userId.toString());
            timingMessageDOUpdate.setPushDateTime(updateTimingMessageReq.getPushDateTime());
            timingMessageDOUpdate.setPushWay(updateTimingMessageReq.getPushWay());
            timingMessageDOUpdate.setReceiver(updateTimingMessageReq.getReceiver());
            timingMessageDOUpdate.setMessage(updateTimingMessageReq.getMessage());
            timingMessageDOUpdate.setPushCycle(updateTimingMessageReq.getPushCycle());
            timingMessageDOUpdate.setCycleUnit(updateTimingMessageReq.getCycleUnit());
            timingMessageDOUpdate.setEffective(updateTimingMessageReq.getEffective());
            timingMessageManager.updateById(timingMessageDOUpdate);
        }
        return true;
    }

    /**
     * 获取消息列表 分页查询
     * @param userId
     * @param pageNum
     * @param pageSize
     * @param pushWayEnum
     * @param effective
     * @return
     */
    public PageInfo<TimingMessageDTO> getTimingMessageList(Long userId, Integer pageNum, Integer pageSize,
                                                           PushStateEnum pushStateEnum, PushWayEnum pushWayEnum,
                                                           Boolean effective){
        PageInfo<TimingMessageDTO> res = new PageInfo<>();
        res.setPageNum(pageNum);
        res.setPageSize(pageSize);
        res.setTotal(0);

        List<TimingMessageDO> timingMessageDOList = timingMessageManager.queryUserTimingMessage(userId, pageSize,
                pageNum, pushStateEnum, pushWayEnum, effective);
        if (CollectionUtils.isEmpty(timingMessageDOList)){
            return res;
        }
        List<TimingMessageDTO> timingMessageDTOS = timingMessageDOList.stream().map(this::convertTimingMessageDTO)
                .collect(Collectors.toList());
        Long count = timingMessageManager.countById(userId, pushStateEnum, pushWayEnum, effective);
        res.setTotal(count);
        res.setList(timingMessageDTOS);
        return res;
    }

    /**
     * 转换
     * @param timingMessageDO
     * @return
     */
    private TimingMessageDTO convertTimingMessageDTO(TimingMessageDO timingMessageDO){
        TimingMessageDTO timingMessageDTO = new TimingMessageDTO();
        timingMessageDTO.setTimingMessageId(timingMessageDO.getId());
        timingMessageDTO.setPushDateTime(timingMessageDO.getPushDateTime());
        timingMessageDTO.setState(timingMessageDO.getState());
        timingMessageDTO.setPushWay(timingMessageDO.getPushWay());
        timingMessageDTO.setReceiver(timingMessageDO.getReceiver());
        timingMessageDTO.setMessage(timingMessageDO.getMessage());
        timingMessageDTO.setPushCycle(timingMessageDO.getPushCycle());
        timingMessageDTO.setCycleUnit(timingMessageDO.getCycleUnit());
        timingMessageDTO.setEffective(timingMessageDO.getEffective());
        return timingMessageDTO;
    }

    /**
     * 自身不保证重复消费，需要上层上锁
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
        String sourceId = pushMessageService.getSourceId(item.getPushWay(), item.getId().toString());
        Boolean sendResult = pushMessageService.sendMessage(sourceId, item.getCreator(), item.getReceiver(),
                pushWayEnum, item.getMessage());
        if (!sendResult) {
            return false;
        }
        Date nextPushTime = this.getNextPushTime(item);
        if (nextPushTime == null) {
            // NULL表示不需要下次推送了，将消息更新为FINISH
            log.info("this message dose not need push next, timingMessage:{}", JSON.toJSONString(item));
            timingMessageManager.updateMessageStateByOriginState(item.getId(),
                    PushStateEnum.FINISH, PushStateEnum.INIT);
        } else {
            // 发送完消息，更新下次发送消息时间
            log.info("message need push next time, timingPushMessageId:{}, nextPushTime:{}",
                    item.getId(), nextPushTime);
            timingMessageManager.updatePushMessageTime(item.getId(), nextPushTime);
        }
        return true;
    }

    /**
     * 获取下一次推送消息时间
     * @param originTimingMessageDO
     * @return
     */
    private Date getNextPushTime(TimingMessageDO originTimingMessageDO) {
        String cycleUnit = originTimingMessageDO.getCycleUnit();
        Date pushDateTime = originTimingMessageDO.getPushDateTime();
        Integer pushCycleCount = originTimingMessageDO.getPushCycle();

        CycleUnitEnum cycleUnitEnum = CycleUnitEnum.getByName(cycleUnit);
        if (cycleUnitEnum == null) {
            log.warn("cycleUnitEnum is null, data error while insertNextMessageDO, originTimingMessageDO:{}",
                    JSON.toJSONString(originTimingMessageDO));
            return null;
        }
        if (cycleUnitEnum == CycleUnitEnum.NONE) {
            log.info("this timing message without cycle, ");
            return null;
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
        return newDate;
    }
}

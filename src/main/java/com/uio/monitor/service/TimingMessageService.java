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

import java.util.Collections;
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
    TimingMessageDOMapper timingMessageDOMapper;
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
        timingMessageDO.setState("INIT"); // 先写为 INIT
        timingMessageDO.setReceiver(addTimingMessageReq.getReceiver());
        timingMessageDO.setMessage(addTimingMessageReq.getMessage());
        timingMessageDO.setPushCycle(addTimingMessageReq.getPushCycle());
        timingMessageDO.setCycleUnit(addTimingMessageReq.getCycleUnit());
        timingMessageDO.setPushWay(addTimingMessageReq.getPushWay());
        timingMessageDO.setEffective(addTimingMessageReq.getEffective());
        timingMessageDO.setGmtCreate(new Date());
        timingMessageDO.setGmtModify(new Date());
        timingMessageDO.setDeleted(false);
        timingMessageDOMapper.insert(timingMessageDO);
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
            timingMessageDOUpdate.setId(userId);
            timingMessageDOUpdate.setGmtCreate(new Date());
            timingMessageDOUpdate.setGmtModify(new Date());
            timingMessageDOUpdate.setModifier("system");
            timingMessageDOUpdate.setCreator(userId.toString());
            timingMessageDOUpdate.setDeleted(updateTimingMessageReq.getDeleted());
            timingMessageDOUpdate.setPushDateTime(updateTimingMessageReq.getPushDateTime());
            timingMessageDOUpdate.setState("INIT");  // 先写成 INIT
            timingMessageDOUpdate.setPushWay(updateTimingMessageReq.getPushWay());
            timingMessageDOUpdate.setReceiver(timingMessageDOUpdate.getReceiver());
            timingMessageDOUpdate.setMessage(updateTimingMessageReq.getMessage());
            timingMessageDOUpdate.setPushCycle(updateTimingMessageReq.getPushCycle());
            timingMessageDOUpdate.setCycleUnit(updateTimingMessageReq.getCycleUnit());
            timingMessageDOUpdate.setEffective(updateTimingMessageReq.getEffective());
        }
        return true;
    }

    /**
     * 获取消息列表 分页查询
     * @param userId
     * @param pageNum
     * @param pageSize
     * @param pushStateEnum
     * @param pushWayEnum
     * @param effective
     * @return
     */
    public PageInfo<TimingMessageDTO> getTimingMessageList(Long userId, Integer pageNum, Integer pageSize, PushStateEnum pushStateEnum, PushWayEnum pushWayEnum, Boolean effective){
        PageInfo<TimingMessageDTO> res = new PageInfo<>();
        res.setPageNum(pageNum);
        res.setPageSize(pageSize);
        res.setTotal(0);

        List<TimingMessageDO> timingMessageDOList = timingMessageManager.queryReadyMessage();
        if (CollectionUtils.isEmpty(timingMessageDOList)){
            return res;
        }
        List<TimingMessageDTO> timingMessageDTOS = timingMessageDOList.stream().map(this::convertTimingMessageDTO).collect(Collectors.toList());
        Long count = timingMessageManager.countById(userId, pageNum, pageSize, pushStateEnum, pushWayEnum, effective);
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
        timingMessageDTO.setTimingMessageId(timingMessageDTO.getTimingMessageId());
        timingMessageDTO.setPushDateTime(timingMessageDTO.getPushDateTime());
        timingMessageDTO.setState("INIT");
        timingMessageDTO.setPushWay(timingMessageDTO.getPushWay());
        timingMessageDTO.setReceiver(timingMessageDTO.getReceiver());
        timingMessageDTO.setMessage(timingMessageDTO.getMessage());
        timingMessageDTO.setPushCycle(timingMessageDTO.getPushCycle());
        timingMessageDTO.setCycleUnit(timingMessageDTO.getCycleUnit());
        timingMessageDTO.setEffective(timingMessageDTO.getEffective());
        return timingMessageDTO;
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

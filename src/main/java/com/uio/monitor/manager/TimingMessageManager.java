package com.uio.monitor.manager;

import com.github.pagehelper.PageHelper;
import com.uio.monitor.common.PushStateEnum;
import com.uio.monitor.common.PushWayEnum;
import com.uio.monitor.entity.PushMessageDO;
import com.uio.monitor.entity.TimingMessageDO;
import com.uio.monitor.entity.TimingMessageDOExample;
import com.uio.monitor.mapper.TimingMessageDOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author han xun
 * Date 2022/3/28 23:53
 * Description:
 */
@Component
public class TimingMessageManager {

    @Autowired
    private TimingMessageDOMapper timingMessageDOMapper;

    /**
     * 查询待发送的消息
     * @return
     */
    public List<TimingMessageDO> queryReadyMessage() {
        TimingMessageDOExample example = new TimingMessageDOExample();
        TimingMessageDOExample.Criteria criteria = example.createCriteria();
        criteria.andStateIn(Arrays.asList(PushStateEnum.INIT.name(), PushStateEnum.PROCESSING.name()));
        criteria.andPushDateTimeLessThanOrEqualTo(new Date());
        criteria.andEffectiveEqualTo(true);
        criteria.andDeletedEqualTo(false);
        return timingMessageDOMapper.selectByExample(example);
    }

    public TimingMessageDO queryById(Long timingMessageId) {
        return timingMessageDOMapper.selectByPrimaryKey(timingMessageId);
    }

    public List<TimingMessageDO> queryUserTimingMessage(Long userId, Integer pageNum, Integer pageSize,
                                                        PushStateEnum pushStateEnum, PushWayEnum pushWayEnum,
                                                        Boolean effective) {
        if (userId == null) {
            return Collections.emptyList();
        }
        // 分页查询
        PageHelper.startPage(pageNum, pageSize);

        TimingMessageDOExample example = new TimingMessageDOExample();
        TimingMessageDOExample.Criteria criteria = example.createCriteria();
        criteria.andDeletedEqualTo(false);
        criteria.andCreatorEqualTo(userId.toString());
        if (pushWayEnum != null) {
            criteria.andPushWayEqualTo(pushWayEnum.name());
        }
        if (effective != null) {
            criteria.andEffectiveEqualTo(effective);
        }
        if (pushStateEnum != null) {
            criteria.andStateEqualTo(pushStateEnum.name());
        }
        return timingMessageDOMapper.selectByExample(example);
    }

    public Long countById (Long userId, PushStateEnum pushStateEnum, PushWayEnum pushWayEnum, Boolean effective){
        if (userId == null) {
            return 0L;
        }
        TimingMessageDOExample example = new TimingMessageDOExample();
        TimingMessageDOExample.Criteria criteria = example.createCriteria();
        criteria.andDeletedEqualTo(false);
        criteria.andCreatorEqualTo(userId.toString());
        if (pushWayEnum != null) {
            criteria.andPushWayEqualTo(pushWayEnum.name());
        }
        if (effective != null) {
            criteria.andEffectiveEqualTo(effective);
        }
        if (pushStateEnum != null) {
            criteria.andStateEqualTo(pushStateEnum.name());
        }
        return timingMessageDOMapper.countByExample(example);
    }

    /**
     * 根据原状态修改状态
     * @param id
     * @param pushState
     * @param oldPushState
     * @return
     */
    public int updateMessageStateByOriginState(Long id, String pushState, String oldPushState) {
        TimingMessageDOExample example = new TimingMessageDOExample();
        TimingMessageDOExample.Criteria criteria = example.createCriteria();
        criteria.andIdEqualTo(id);
        criteria.andStateEqualTo(oldPushState);
        TimingMessageDO timingMessageDO = new TimingMessageDO();
        timingMessageDO.setGmtModify(new Date());
        timingMessageDO.setModifier("system");
        timingMessageDO.setState(pushState);

        return timingMessageDOMapper.updateByExampleSelective(timingMessageDO, example);
    }

    public void insertMessage(TimingMessageDO timingMessageDO) {
        timingMessageDO.setGmtModify(new Date());
        timingMessageDO.setGmtCreate(new Date());
        timingMessageDO.setDeleted(false);
        timingMessageDOMapper.insert(timingMessageDO);
    }

    /**
     * 更新推送时间
     * @param id
     * @param updatePushTime
     * @return
     */
    public int updatePushMessageTime(Long id, Date updatePushTime) {
        TimingMessageDOExample example = new TimingMessageDOExample();
        TimingMessageDOExample.Criteria criteria = example.createCriteria();
        criteria.andIdEqualTo(id);
        TimingMessageDO timingMessageDO = new TimingMessageDO();
        timingMessageDO.setGmtModify(new Date());
        timingMessageDO.setModifier("system");
        timingMessageDO.setPushDateTime(updatePushTime);
        timingMessageDO.setState(PushStateEnum.PROCESSING.name());
        return timingMessageDOMapper.updateByExampleSelective(timingMessageDO, example);
    }

    public void updateById(TimingMessageDO timingMessageDO) {
        timingMessageDO.setGmtModify(new Date());
        timingMessageDOMapper.updateByPrimaryKeySelective(timingMessageDO);
    }

    /**
     * 根据 id 删除
     * @param messageId
     */
    public void deleteById(Long messageId){
        TimingMessageDO timingMessageDO = new TimingMessageDO();
        timingMessageDO.setId(messageId);
        timingMessageDO.setGmtModify(new Date());
        timingMessageDO.setDeleted(true);
        timingMessageDOMapper.updateByPrimaryKeySelective(timingMessageDO);
    }
}

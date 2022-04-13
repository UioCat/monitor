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
        criteria.andStateEqualTo(PushStateEnum.INIT.name());
        criteria.andPushDateTimeLessThanOrEqualTo(new Date());
        criteria.andEffectiveEqualTo(true);
        criteria.andDeletedEqualTo(false);
        return timingMessageDOMapper.selectByExample(example);
    }

    public int updateMessageState(Long id, PushStateEnum pushStateEnum, PushStateEnum oldPushStateEnum) {
        TimingMessageDOExample example = new TimingMessageDOExample();
        TimingMessageDOExample.Criteria criteria = example.createCriteria();
        criteria.andIdEqualTo(id);
        criteria.andStateEqualTo(oldPushStateEnum.name());
        TimingMessageDO timingMessageDO = new TimingMessageDO();
        timingMessageDO.setGmtModify(new Date());
        timingMessageDO.setModifier("system");
        timingMessageDO.setState(pushStateEnum.name());

        return timingMessageDOMapper.updateByExampleSelective(timingMessageDO, example);
    }

    public void insertMessage(TimingMessageDO messageDO) {
        timingMessageDOMapper.insert(messageDO);
    }
    /**
     * 根据 id 删除
     * @param id
     * @param modifier
     */
    public void deleteById(Long id,String modifier){
        TimingMessageDO timingMessageDO = new TimingMessageDO();
        timingMessageDO.setId(id);
        timingMessageDO.setGmtModify(new Date());
        timingMessageDO.setModifier(modifier);
        timingMessageDO.setDeleted(true);
        timingMessageDOMapper.updateByPrimaryKeySelective(timingMessageDO);
    }

    public Long countById (Long userId, Integer pageNum, Integer pageSize, PushStateEnum pushStateEnum, PushWayEnum pushWayEnum, Boolean effective){
        PageHelper.startPage(pageNum, pageSize);
        TimingMessageDOExample example = new TimingMessageDOExample();
        TimingMessageDOExample.Criteria criteria = example.createCriteria();
        criteria.andDeletedEqualTo(false);
        return timingMessageDOMapper.countByExample(example);
    }

}

package com.uio.monitor.manager;

import com.uio.monitor.common.PushStateEnum;
import com.uio.monitor.entity.PushMessageDO;
import com.uio.monitor.entity.TimingMessageDO;
import com.uio.monitor.entity.TimingMessageDOExample;
import com.uio.monitor.mapper.TimingMessageDOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
        criteria.andDeletedEqualTo(false);
        return timingMessageDOMapper.selectByExample(example);
    }

    public void updateMessageState(Long id, PushStateEnum pushStateEnum) {
        return;
    }

    public void insertMessage(PushMessageDO messageDO) {
        return;
    }

}

package com.uio.monitor.manager;

import com.uio.monitor.common.PushStateEnum;
import com.uio.monitor.entity.PushMessageDO;
import com.uio.monitor.entity.PushMessageDOExample;
import com.uio.monitor.mapper.PushMessageDOMapper;
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
public class PushMessageManager {

    @Autowired
    private PushMessageDOMapper pushMessageDOMapper;

    /**
     * 查询待发送的消息
     * @return
     */
    public List<PushMessageDO> queryReadyMessage() {
        PushMessageDOExample example = new PushMessageDOExample();
        PushMessageDOExample.Criteria criteria = example.createCriteria();
        criteria.andStateEqualTo(PushStateEnum.INIT.name());
        criteria.andPushDateTimeLessThanOrEqualTo(new Date());
        criteria.andDeletedEqualTo(false);
        return pushMessageDOMapper.selectByExample(example);
    }

    public void updateMessageState(Long id, PushStateEnum pushStateEnum) {
        return;
    }

    public void insertMessage(PushMessageDO messageDO) {
        return;
    }

}

package com.uio.monitor.manager;

import com.uio.monitor.common.PushStateEnum;
import com.uio.monitor.entity.PushMessageDO;
import com.uio.monitor.entity.PushMessageDOExample;
import com.uio.monitor.mapper.PushMessageDOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @author han xun
 * Date 2022/4/5 00:12
 * Description:
 */
@Repository
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
        criteria.andDeletedEqualTo(false);
        return pushMessageDOMapper.selectByExample(example);
    }
  
    public void insert(PushMessageDO pushMessageDO) {
        pushMessageDO.setGmtModify(new Date());
        pushMessageDO.setGmtCreate(new Date());
        pushMessageDOMapper.insert(pushMessageDO);
    }
}

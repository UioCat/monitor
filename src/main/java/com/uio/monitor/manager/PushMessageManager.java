package com.uio.monitor.manager;

import com.uio.monitor.entity.PushMessageDO;
import com.uio.monitor.mapper.PushMessageDOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * @author han xun
 * Date 2022/4/5 00:12
 * Description:
 */
@Repository
public class PushMessageManager {

    @Autowired
    private PushMessageDOMapper pushMessageDOMapper;

    public void insert(PushMessageDO pushMessageDO) {
        pushMessageDO.setGmtModify(new Date());
        pushMessageDO.setGmtCreate(new Date());
        pushMessageDOMapper.insert(pushMessageDO);
    }
}

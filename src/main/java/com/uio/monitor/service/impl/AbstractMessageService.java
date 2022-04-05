package com.uio.monitor.service.impl;

import com.uio.monitor.manager.PushMessageManager;
import com.uio.monitor.service.PushMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author han xun
 * Date 2022/4/5 00:09
 * Description:
 */
@Service
public abstract class AbstractMessageService implements PushMessageService {

    @Autowired
    private PushMessageManager pushMessageManager;

    @Override
    public Boolean insertPushMessageData() {
        return null;
    }
}

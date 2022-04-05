package com.uio.monitor.service;

import com.uio.monitor.controller.req.AddTimingMessageReq;
import com.uio.monitor.manager.TimingMessageManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * @author han xun
 * Date 2022/4/4 23:46
 * Description: 定时消息服务
 */
@Service
public class TimingMessageService {

    @Autowired
    private TimingMessageManager timingMessageManager;

    /**
     * 加入一条消息
     * @param userId
     * @param addTimingMessageReq
     * @return
     */
    public Boolean addTimingMessage(Long userId, AddTimingMessageReq addTimingMessageReq) {
        return null;
    }

}

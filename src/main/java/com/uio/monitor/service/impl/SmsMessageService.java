package com.uio.monitor.service.impl;

import com.uio.monitor.common.PushWayEnum;
import com.uio.monitor.service.PushMessageService;
import org.springframework.stereotype.Service;

/**
 * @author han xun
 * Date 2022/4/5 00:03
 * Description:
 */
@Service("smsMessageService")
public class SmsMessageService extends AbstractMessageService {


    @Override
    public Boolean sendMessage(String sourceId, String sender, String receiver, PushWayEnum pushWayEnum, String message) {
        return null;
    }
}

package com.uio.monitor.service.impl;

import com.uio.monitor.common.PushWayEnum;
import com.uio.monitor.service.PushMessageService;
import org.springframework.stereotype.Service;

/**
 * @author han xun
 * Date 2022/4/5 00:01
 * Description:
 */
@Service("emailMessageService")
public class EmailMessageService extends AbstractMessageService {

    @Override
    public Boolean sendMessage(String sender, String receiver, PushWayEnum pushWayEnum, String message) {
        return null;
    }
}

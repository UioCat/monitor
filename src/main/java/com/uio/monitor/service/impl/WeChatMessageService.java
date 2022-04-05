package com.uio.monitor.service.impl;

import com.uio.monitor.common.PushWayEnum;
import org.springframework.stereotype.Service;

/**
 * @author han xun
 * Date 2022/4/4 23:59
 * Description:
 */
@Service("weChatMessageService")
public class WeChatMessageService extends AbstractMessageService {

    @Override
    public Boolean sendMessage(String sender, String receiver, PushWayEnum pushWayEnum, String message) {
        return null;
    }
}

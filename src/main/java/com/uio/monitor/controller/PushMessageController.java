package com.uio.monitor.controller;

import com.alibaba.fastjson.JSONObject;
import com.uio.monitor.common.BackMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author han xun
 * Date 2022/4/9 10:35
 * Description: 推送消息服务 for 拉取推送消息
 */
@RestController
public class PushMessageController {

    /**
     * 获取一条待发送的消息
     * @return
     */
    @GetMapping("/")
    public BackMessage<JSONObject> getPushMessage() {
        return null;
    }

    /**
     * 回调消息发送状态
     * @return
     */
    @PostMapping("/")
    public BackMessage<Boolean> setPushMessageResult() {
        return null;
    }

}

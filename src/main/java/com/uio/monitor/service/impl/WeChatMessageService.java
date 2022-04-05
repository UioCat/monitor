package com.uio.monitor.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.uio.monitor.common.PushStateEnum;
import com.uio.monitor.common.PushWayEnum;
import com.uio.monitor.entity.PushMessageDO;
import com.uio.monitor.utils.URLConnection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.Optional;

/**
 * @author han xun
 * Date 2022/4/4 23:59
 * Description:
 */
@Service("weChatMessageService")
@Slf4j
public class WeChatMessageService extends AbstractMessageService {

    private final static String WECHAT_MESSAGE = "message";
    private final static String VERIFY_CODE = "verifyCode";

    @Value("${push-message.wechat-url}")
    private String WECHAT_PUSH_MESSAGE_URL;

    @Override
    public Boolean sendMessage(String sender, String receiver, PushWayEnum pushWayEnum, String message) {
        if (ObjectUtils.isEmpty(message)) {
            log.warn("push to wechat, but message is empty, receiver:{}, sender:{}", receiver, sender);
            return false;
        }

        JSONObject jsonObject = JSON.parseObject(message);
        String sendMessage = Optional.ofNullable(jsonObject.get(WECHAT_MESSAGE)).orElse("").toString();
        String verifyCode = Optional.ofNullable(jsonObject.get(VERIFY_CODE)).orElse("").toString();
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("toSend", receiver);
        jsonParam.put("message", sendMessage);
        jsonParam.put("verifyCode", verifyCode);

        PushMessageDO pushMessageDO = new PushMessageDO();
        pushMessageDO.setGmtCreate(new Date());
        pushMessageDO.setGmtModify(new Date());
        pushMessageDO.setCreator(sender);
        pushMessageDO.setModifier(sender);
        pushMessageDO.setDeleted(false);
        pushMessageDO.setState(PushStateEnum.FINISH.name());
        pushMessageDO.setPushWay(PushWayEnum.WECHAT.name());
        pushMessageDO.setReceiver(receiver);
        pushMessageDO.setMessage(sendMessage);

        String response = URLConnection.doPost(WECHAT_PUSH_MESSAGE_URL, jsonParam.toString());
        JSONObject resJson = JSON.parseObject(response);
        int code = Integer.parseInt(Optional.ofNullable(resJson.get("code")).orElse("").toString());
        if (code == 200) {
            log.info("send message to wechat receiver:{}, message:{}, response:{}", receiver, message, response);
            String info = Optional.ofNullable(resJson.get("info")).orElse("").toString();
            if (Boolean.TRUE.toString().equals(info)) {
                super.insertPushMessageData(pushMessageDO);
                return true;
            }
        }
        pushMessageDO.setState(PushStateEnum.FAILED.name());
        super.insertPushMessageData(pushMessageDO);
        log.warn("send message fail, receiver:{}, message:{}, response:{}", receiver, message, response);
        return false;
    }
}

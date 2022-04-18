package com.uio.monitor.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.uio.monitor.common.BackEnum;
import com.uio.monitor.common.CustomException;
import com.uio.monitor.common.PushStateEnum;
import com.uio.monitor.common.PushWayEnum;
import com.uio.monitor.entity.PushMessageDO;
import com.uio.monitor.utils.URLConnection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

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

    @Value("${pushMessage.wechatUrl:}")
    private String WECHAT_PUSH_MESSAGE_URL;
    @Value("${pushMessage.wechatSecretKey:}")
    private String verifyCode;

    @Override
    public Boolean sendMessage(String sourceId, String sender, String receiver, PushWayEnum pushWayEnum, String message) {
        if (ObjectUtils.isEmpty(message)) {
            log.warn("push to wechat, but message is empty, sourceId:{}, receiver:{}, sender:{}",
                    sourceId, receiver, sender);
            return false;
        }

        JSONObject jsonParam = new JSONObject();
        jsonParam.put("toSend", receiver);
        jsonParam.put("message", message);
        jsonParam.put("verifyCode", verifyCode);

        PushMessageDO pushMessageDO = new PushMessageDO();
        pushMessageDO.setGmtCreate(new Date());
        pushMessageDO.setGmtModify(new Date());
        pushMessageDO.setCreator(sender);
        pushMessageDO.setModifier(sender);
        pushMessageDO.setDeleted(false);
        pushMessageDO.setState(PushStateEnum.FINISH.name());
        pushMessageDO.setPushWay(PushWayEnum.WECHAT.name());
        pushMessageDO.setSender(sender);
        pushMessageDO.setReceiver(receiver);
        pushMessageDO.setMessage(message);
        pushMessageDO.setSourceid(sourceId);
        String response = null;
        if (!StringUtils.isEmpty(WECHAT_PUSH_MESSAGE_URL)) {
            response = URLConnection.doPost(WECHAT_PUSH_MESSAGE_URL, jsonParam.toString());
            JSONObject resJson = JSON.parseObject(response);
            if (response != null) {
                int code = Integer.parseInt(Optional.ofNullable(resJson.get("code")).orElse("").toString());
                if (code == 200) {
                    log.info("send message to wechat receiver:{}, message:{}, response:{}", receiver, message, response);
                    String info = Optional.ofNullable(resJson.get("info")).orElse("").toString();
                    if (Boolean.TRUE.toString().equals(info)) {
                        // 发送成功，插入成功状态的数据
                        super.insertPushMessageData(pushMessageDO);
                        log.info("insert push message data success sourceId:{}", sourceId);
                        return true;
                    }
                } else {
                    log.warn("send message to wechat fail, code:{} resJSON:{}", code, JSON.toJSONString(resJson));
                }
            }
        } else {
            log.warn("WECHAT_PUSH_MESSAGE_URL is empty:{}, send message param:{}",
                    WECHAT_PUSH_MESSAGE_URL, jsonParam.toString());
        }
        // 发送不成功的所有情况，都插入FAILED
        pushMessageDO.setState(PushStateEnum.FAILED.name());
        super.insertPushMessageData(pushMessageDO);
        log.warn("send message fail, receiver:{}, message:{}, jsonParam:{}, response:{} url:{}",
                receiver, message, jsonParam, response, WECHAT_PUSH_MESSAGE_URL);
        return false;
    }
}

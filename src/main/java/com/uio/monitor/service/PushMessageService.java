package com.uio.monitor.service;


import com.uio.monitor.common.PushWayEnum;
import com.uio.monitor.entity.PushMessageDO;

/**
 * @author han xun
 * Date 2022/3/30 20:40
 * Description: 推送消息服务
 */
public interface PushMessageService {

    /**
     * 发送消息
     * @return
     */
    Boolean sendMessage(String sourceId, String sender, String receiver, PushWayEnum pushWayEnum, String message);

    /**
     * 插入推送消息记录
     * @return
     */
    Boolean insertPushMessageData(PushMessageDO pushMessageDO);

    default String getSourceId(String pushWayName, String id) {
        return pushWayName + "_" + id;
    }
}

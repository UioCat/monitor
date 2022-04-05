package com.uio.monitor.service;


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
    Boolean sendMessage();

    /**
     * 插入推送消息记录
     * @return
     */
    Boolean insertPushMessageData();
}

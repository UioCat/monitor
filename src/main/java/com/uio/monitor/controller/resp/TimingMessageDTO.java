package com.uio.monitor.controller.resp;

import lombok.Data;

import java.util.Date;

/**
 * @author han xun
 * Date 2022/4/9 10:12
 * Description:
 */
@Data
public class TimingMessageDTO {

    private Long timingMessageId;

    /**
     * 消息推送时间
     */
    private Date pushDateTime;

    /**
     * 消息推送状态
     */
    private String state;
    /**
     * 消息推送途径
     */
    private String pushWay;

    private String receiver;
    /**
     * 消息体
     */
    private String message;
    /**
     * 推送周期
     */
    private Integer pushCycle;
    /**
     * 周期单位
     */
    private String cycleUnit;
    /**
     * 是否生效
     */
    private Boolean effective;

}

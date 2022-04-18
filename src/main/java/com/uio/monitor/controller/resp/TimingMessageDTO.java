package com.uio.monitor.controller.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author han xun
 * Date 2022/4/9 10:12
 * Description:
 */
@Data
public class TimingMessageDTO {

    /**
     * 定时消息ID
     */
    private Long timingMessageId;
    /**
     * 消息推送时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date pushDateTime;
    /**
     * 消息推送状态
     */
    private String state;
    /**
     * 消息推送途径
     */
    private String pushWay;
    /**
     * 接受人
     */
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

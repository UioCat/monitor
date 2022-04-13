package com.uio.monitor.controller.req;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author han xun
 * Date 2022/4/3 23:15
 * Description:
 */
@Data
public class AddTimingMessageReq {
    /**
     * 推送时间
     */
    @NotNull
    private Date pushDateTime;
    /**
     * 推送途径
     */
    @NotNull
    private String pushWay;
    /**
     * 接受人
     */
    @NotEmpty
    private String receiver;
    /**
     * 内容
     */
    @NotEmpty
    private String message;
    /**
     * 周期单元
     * {@link com.uio.monitor.common.CycleUnitEnum}
     */
    @NotNull
    private String cycleUnit;
    /**
     * 推送周期
     */
    private Integer pushCycle;
    /**
     * 是否生效
     */
    @NotNull
    private Boolean effective;
    /**
     * 创建消息者
     */
    @NotEmpty
    private String creator;
    /**
     * 消息修改者
     */
    @NotEmpty
    private String modifier;
    /**
     * 用户ID
     */
    private Long userId;
}

package com.uio.monitor.controller.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

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
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
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
}

package com.uio.monitor.controller.req;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author han xun
 * Date 2022/4/3 23:22
 * Description:
 */
@Data
public class UpdateTimingMessageReq extends AddTimingMessageReq {
    /**
     * 是否需要删除，TRUE是直接删除改定时消息
     */
    private Boolean deleted;
    /**
     * 消息更新者
     */
    private String Modifier;

    private Long MessageId;
}

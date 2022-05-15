package com.uio.monitor.controller.req;

import lombok.Data;

/**
 * @author han xun
 * Date 2022/5/5 13:44
 * Description:
 */
@Data
public class ReceiveMessageReq {

    private String source;

    private String sourceId;

    private String sender;

    private String messageContent;
}

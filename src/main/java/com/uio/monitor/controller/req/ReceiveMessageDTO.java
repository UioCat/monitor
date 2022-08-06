package com.uio.monitor.controller.req;

import lombok.Data;

/**
 * @author han xun
 * Date 2022/8/5 20:11
 * Description:
 */
@Data
public class ReceiveMessageDTO {

    private String sourceId;

    private String sender;

    private String messageContent;
}

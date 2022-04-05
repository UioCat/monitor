package com.uio.monitor.common;

/**
 * @author han xun
 * Date 2022/3/25 01:28
 * Description:
 */
public enum PushWayEnum {

    MAIL("emailMessageService"),
    WECHAT("weChatMessageService"),
    MESSAGE("smsMessageService"),
    ;

    private String serviceName;

    PushWayEnum(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}

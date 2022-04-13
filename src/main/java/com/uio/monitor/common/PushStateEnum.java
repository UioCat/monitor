package com.uio.monitor.common;

/**
 * @author han xun
 * Date 2022/3/25 22:54
 * Description:
 */
public enum PushStateEnum {

    INIT,
    PROCESSING,
    FINISH,
    FAILED,
    ;
    public static PushStateEnum getByName(String name) {
        try {
            return PushStateEnum.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

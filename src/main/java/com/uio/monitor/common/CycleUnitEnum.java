package com.uio.monitor.common;

/**
 * @author han xun
 * Date 2022/3/25 01:29
 * Description:
 */
public enum CycleUnitEnum {

    NONE,
    MINUTE,
    HOUR,
    DAY,
    WEEK,
    MONTH,
    YEAR,
    ;

    public static CycleUnitEnum getByName(String name) {
        try {
            return CycleUnitEnum.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

package com.uio.monitor.common;

/**
 * @author han xun
 * Date 2021/12/31 19:44
 * Description:
 */
public enum BillTypeEnum {

    /**
     * 支出
     */
    CONSUME,
    /**
     * 收入
     */
    INCOME,
    ;

    public static BillTypeEnum getByName(String name) {
        try {
            return BillTypeEnum.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

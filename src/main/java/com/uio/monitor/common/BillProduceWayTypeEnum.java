package com.uio.monitor.common;

/**
 * @author han xun
 * Date 2022/1/1 13:37
 * Description:
 */
public enum BillProduceWayTypeEnum {

    ALI_PAY,
    WECHAT,
    BANK_CARD,
    ;

    public static BillProduceWayTypeEnum getByName(String name) {
        try {
            return BillProduceWayTypeEnum.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

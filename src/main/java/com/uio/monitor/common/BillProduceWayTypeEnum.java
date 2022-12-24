package com.uio.monitor.common;

/**
 * @author han xun
 * Date 2022/1/1 13:37
 * Description:
 */
public enum BillProduceWayTypeEnum {

    ALI_PAY("支付宝"),
    WECHAT("微信"),
    BANK_CARD("银行卡"),
    CASH("现金"),
    CREDIT_CARD("信用卡"),
    ;

    private String desc;

    BillProduceWayTypeEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public static BillProduceWayTypeEnum getByDesc(String desc) {
        for (BillProduceWayTypeEnum billProduceWayTypeEnum : BillProduceWayTypeEnum.values()) {
            if (billProduceWayTypeEnum.getDesc().equals(desc)) {
                return billProduceWayTypeEnum;
            }
        }
        return null;
    }

    public static BillProduceWayTypeEnum getByName(String name) {
        try {
            return BillProduceWayTypeEnum.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

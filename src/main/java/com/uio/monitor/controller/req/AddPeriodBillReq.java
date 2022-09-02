package com.uio.monitor.controller.req;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author han xun
 * Date 2022/8/31 00:31
 * Description:
 */
@Data
public class AddPeriodBillReq {
    @NotNull
    private Integer generateDay;
    @NotNull
    private Integer generateCount;
    /**
     * 账单类型 {@link com.uio.monitor.common.BillTypeEnum}
     */
    @NotNull
    private String billType;
    /**
     * 账单生产途径 {@link com.uio.monitor.common.BillProduceWayTypeEnum}
     */
    private String produceWayType;
    /**
     * 金额
     */
    @NotNull
    private String amount;
    /**
     * 描述
     */
    private String desc;
    /**
     * 账单分类类型
     */
    @NotNull
    private String type;
}

package com.uio.monitor.controller.resp;

import lombok.Data;

/**
 * @author han xun
 * Date 2022/10/11 23:05
 * Description:
 */
@Data
public class PeriodBillDTO {
    /**
     * 每月产生日期
     */
    private Integer generateDay;
    /**
     * 剩余持续月份
     */
    private Integer generateCount;
    /**
     * 金额
     */
    private String amount;
    /**
     * 用途
     */
    private String description;
    /**
     * 类别
     */
    private String category;
}

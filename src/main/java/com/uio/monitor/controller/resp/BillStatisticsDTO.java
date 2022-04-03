package com.uio.monitor.controller.resp;

import lombok.Data;

/**
 * @author han xun
 * Date 2022/2/13 13:18
 * Description:
 */
@Data
public class BillStatisticsDTO {

    /**
     * 种类
     */
    private String category;
    /**
     * 金额
     */
    private Double amount;
}

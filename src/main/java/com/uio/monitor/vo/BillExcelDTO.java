package com.uio.monitor.vo;

import lombok.Data;


/**
 * @author han xun
 * Date 2022/8/28 12:58
 * Description: 读/写 ExcelDTO
 */
@Data
public class BillExcelDTO {

    /**
     * 产生时间
     */
    private String produceTime;
    /**
     * 金额
     */
    private String amount;
    /**
     * 详细描述
     */
    private String description;
    /**
     * 类型
     */
    private String category;
    /**
     * 产生途径
     */
    private String productWay;
    /**
     * 是否为大货
     */
    private String largeCargo;
}

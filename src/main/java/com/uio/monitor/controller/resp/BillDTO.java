package com.uio.monitor.controller.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author han xun
 * Date 2022/1/3 17:25
 * Description:
 */
@Data
public class BillDTO {
    /**
     * 账单ID
     */
    private Long billId;
    /**
     * 产生时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date produceTime;
    /**
     * 账单类型 {@link com.uio.monitor.common.BillTypeEnum}
     */
    private String billType;
    /**
     * 产生途径 {@link com.uio.monitor.common.BillProduceWayTypeEnum}
     */
    private String produceWayType;

    private BigDecimal amount;

    private String desc;

    private String category;
    /**
     * 是否为大件
     * true：大件
     */
    private Boolean largeItem;
}

package com.uio.monitor.controller.resp;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @author han xun
 * Date 2022/2/12 02:10
 * Description:
 */
@Data
public class BillConfigDTO {

    /**
     * 种类
     */
    @NotEmpty
    private String category;
    /**
     * 价格区间
     * ex：1-20
     */
    private String priceScope;
    /**
     * 配置id
     */
    private Long configId;

}

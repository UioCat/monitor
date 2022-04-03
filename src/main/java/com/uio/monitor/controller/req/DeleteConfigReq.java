package com.uio.monitor.controller.req;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author han xun
 * Date 2022/1/3 15:09
 * Description:
 */
@Data
public class DeleteConfigReq {

    @NotNull
    private Long configId;
}

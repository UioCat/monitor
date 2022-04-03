package com.uio.monitor.controller.req;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author han xun
 * Date 2022/1/2 12:58
 * Description:
 */
@Data
public class LoginReq {

    @NotNull
    private String account;
    @NotNull
    private String password;
}

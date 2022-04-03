package com.uio.monitor.controller.req;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

/**
 * @author han xun
 * Date 2022/1/2 13:23
 * Description:
 */
@Data
public class RegisterReq {
    @NotEmpty(message = "账号不允许为空")
    @Length(min = 6, max = 20, message = "账号长度需要在6～20之间")
    private String account;
    @NotEmpty
    @Length(min = 6, max = 20, message = "密码长度需要在6～20之间")
    private String password;
    @NotEmpty
    private String username;
    @NotEmpty
    private String verifyCode;
}

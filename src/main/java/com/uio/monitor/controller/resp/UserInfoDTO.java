package com.uio.monitor.controller.resp;

import lombok.Data;

/**
 * @author han xun
 * Date 2022/2/12 01:37
 * Description:
 */
@Data
public class UserInfoDTO {
    /**
     * 用户名
     */
    private String username;
    /**
     * 用户头像照片URL
     */
    private String headImage;
}

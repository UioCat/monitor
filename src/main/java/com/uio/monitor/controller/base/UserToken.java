package com.uio.monitor.controller.base;

import lombok.Data;

import java.io.Serializable;

/**
 * @author han xun
 * Date 2021/10/14 22:53
 * Description: 用户Token信息
 */
@Data
public class UserToken implements Serializable {

    private static final long serialVersionUID = -4596822031364325529L;

    /**
     * 用户ID
     */
    private Long id;
}

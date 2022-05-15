package com.uio.monitor.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author han xun
 * Date 2022/4/18 14:02
 * Description: 权限控制
 */
@Data
public class PermissionDO {

    private Long id;

    private Date gmtCreate;

    private Date gmtModify;

    private String creator;

    private String modifier;

    private Boolean deleted;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 权限标识
     */
    private String permissionTag;


}

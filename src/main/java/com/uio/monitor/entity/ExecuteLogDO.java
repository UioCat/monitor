package com.uio.monitor.entity;

import java.util.Date;

/**
 * @author han xun
 * Date 2022/3/25 22:49
 * Description: 执行日志表
 */
public class ExecuteLogDO {

    private Long id;

    private Date gmtCreate;

    private Date gmtModify;

    private String creator;

    private String modifier;

    private Boolean deleted;

    // todo 设计执行日志表

}

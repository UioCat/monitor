package com.uio.monitor.vo;

import lombok.Data;

/**
 * @author uio
 * Date 2021/5/17 下午1:10
 * Description:
 */
@Data
public class ServerMessage {

    /**
     * 服务器名称
     */
    private String name;

    /**
     * 服务器账号
     */
    private String userName;

    /**
     * 服务器密码
     */
    private String password;

    /**
     * 端口号
     */
    private String port;

    /**
     * tomcat执行脚本路径
     */
    private String route;

    /**
     * 服务器ip
     */
    private String ip;
}

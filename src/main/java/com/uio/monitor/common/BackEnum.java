package com.uio.monitor.common;

/**
 * @author uio
 */
public enum BackEnum {

    /**
     * 服务器异常
     */
    UNKNOWN_ERROR(500, "未知错误"),

    /**
     * 请求数据成功，无异常情况
     */
    REQUEST_SUCCESS(200, "请求成功"),

    /**
     * 无法获得用户session信息，或用户session信息不存在
     */
    UNAUTHORIZED(401, "请先登陆"),

    /**
     * 前端数据参数错误
     */
    PARAM_ERROR(400, "参数错误"),

    /**
     * 数据不符合要求
     */
    DATA_ERROR(400, "数据错误"),


    MEDIA_TYPE_ERROR(415, "数据格式错误"),

    /**
     * 请求方式错误，get/post等
     */
    REQUEST_METHOD_ERROR(405, "请求方式错误"),

    /**
     * 密码错误
     */
    PWD_ERROR(401, "密码错误"),

    /**
     * 用户名不存在
     */
    NO_USER(1001, "用户不存在"),

    /**
     * 账号长度过短，一般用于注册
     */
    ACCOUNT_LESS(1002, "账号小于6位"),

    /**
     * 密码长度过短，一般用于注册
     */
    PASSWORD_LESS(1003, "密码过短"),

    /**
     * 数据库已存在该数据
     */
    REPETITION(1004, "数据重复添加"),

    ACCOUNT_EXIST(1005, "账号已存在"),

    UNAUTHORIZED_REGISTER(1006, "不允许注册"),
    ;


    private Integer code;
    private String message;


    BackEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public Integer getCode() {
        return code;
    }

}

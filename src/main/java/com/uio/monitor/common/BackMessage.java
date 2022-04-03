package com.uio.monitor.common;


/**
 * @author uio
 */
public class BackMessage<T> {

    private Integer code;
    private String message;
    private T info;

    public static BackMessage<Void> success() {
        return new BackMessage<Void>(BackEnum.REQUEST_SUCCESS);
    }

    public static <T> BackMessage<T> success(T context) {
        return new BackMessage<T>(BackEnum.REQUEST_SUCCESS, context);
    }

    public BackMessage(BackEnum backEnum) {
        this.code = backEnum.getCode();
        this.message = backEnum.getMessage();
    }

    public BackMessage(BackEnum backEnum, T t) {
        this.code = backEnum.getCode();
        this.message = backEnum.getMessage();
        this.info = t;
    }

    public BackMessage(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getInfo() {
        return info;
    }

    public void setInfo(T info) {
        this.info = info;
    }
}

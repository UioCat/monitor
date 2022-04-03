package com.uio.monitor.common;

import java.io.Serializable;

public class CustomException extends RuntimeException implements Serializable {

    private static final long serialVersionUID = -8244995466985754075L;

    /**
     * 错误码
     */
    private int errorCode;

    /**
     * 错误信息
     */
    private String errorMsg;


    public CustomException() {
    }

    public CustomException(int errorCode, String errorMsg) {
        super();
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public CustomException(String message, int errorCode, String errorMsg) {
        super(message);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public CustomException(String message, Throwable cause, int errorCode, String errorMsg) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public CustomException(Throwable cause, int errorCode, String errorMsg) {
        super(cause);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public CustomException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, int errorCode, String errorMsg) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public CustomException(String errorMsg) {
        super(errorMsg);
        this.errorCode = BackEnum.UNKNOWN_ERROR.getCode();
        this.errorMsg = errorMsg;
    }

    public CustomException(BackEnum backEnum) {
        this.errorCode = backEnum.getCode();
        this.errorMsg = backEnum.getMessage();
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}

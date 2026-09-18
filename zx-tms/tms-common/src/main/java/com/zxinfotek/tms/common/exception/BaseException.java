package com.zxinfotek.tms.common.exception;

public abstract class BaseException extends RuntimeException {

    private final transient ErrorCode errorCode;
    private final transient Object detail;

    protected BaseException(ErrorCode errorCode) {
        this(errorCode, errorCode.getMessage(), null);
    }

    protected BaseException(ErrorCode errorCode, String message) {
        this(errorCode, message, null);
    }

    protected BaseException(ErrorCode errorCode, String message, Object detail) {
        super(message);
        this.errorCode = errorCode;
        this.detail = detail;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Object getDetail() {
        return detail;
    }
}

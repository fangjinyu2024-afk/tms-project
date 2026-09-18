package com.zxinfotek.tms.common.exception;

/** 业务校验失败，HTTP 400。 */
public class BizException extends BaseException {

    public BizException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BizException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public BizException(ErrorCode errorCode, String message, Object detail) {
        super(errorCode, message, detail);
    }
}

package com.zxinfotek.tms.common.exception;

/** 资源不存在，HTTP 404。 */
public class NotFoundException extends BaseException {

    public NotFoundException() {
        super(CommonErrorCode.COMMON_003);
    }

    public NotFoundException(String message) {
        super(CommonErrorCode.COMMON_003, message);
    }

    public NotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}

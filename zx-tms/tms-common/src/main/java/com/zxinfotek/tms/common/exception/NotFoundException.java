package com.zxinfotek.tms.common.exception;

/** 资源不存在，HTTP 404。 */
public class NotFoundException extends BaseException {

    public NotFoundException() {
        super(CommonErrorCode.COMMON_003);
    }

    public NotFoundException(String messageKey, Object... messageArgs) {
        super(CommonErrorCode.COMMON_003, messageKey, messageArgs);
    }

    public NotFoundException(ErrorCode errorCode, String messageKey, Object... messageArgs) {
        super(errorCode, messageKey, messageArgs);
    }
}

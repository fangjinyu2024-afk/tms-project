package com.zxinfotek.tms.common.exception;

/** 状态冲突或乐观锁失败，HTTP 409。 */
public class ConflictException extends BaseException {

    public ConflictException() {
        super(CommonErrorCode.COMMON_002);
    }

    public ConflictException(String messageKey, Object... messageArgs) {
        super(CommonErrorCode.COMMON_002, messageKey, messageArgs);
    }

    public ConflictException(ErrorCode errorCode, String messageKey, Object... messageArgs) {
        super(errorCode, messageKey, messageArgs);
    }
}

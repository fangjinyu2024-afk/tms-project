package com.zxinfotek.tms.common.exception;

/** 状态冲突或乐观锁失败，HTTP 409。 */
public class ConflictException extends BaseException {

    public ConflictException() {
        super(CommonErrorCode.COMMON_002);
    }

    public ConflictException(String message) {
        super(CommonErrorCode.COMMON_002, message);
    }

    public ConflictException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}

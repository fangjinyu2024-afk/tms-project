package com.zxinfotek.tms.common.exception;

/** 无权限或超出数据范围，HTTP 403。 */
public class PermissionException extends BaseException {

    public PermissionException(ErrorCode errorCode) {
        super(errorCode);
    }

    public PermissionException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public PermissionException(ErrorCode errorCode, String message, Object detail) {
        super(errorCode, message, detail);
    }
}

package com.zxinfotek.tms.common.exception;

/** 无权限或超出数据范围，HTTP 403。 */
public class PermissionException extends BaseException {

    public PermissionException(ErrorCode errorCode) {
        super(errorCode);
    }

    public PermissionException(ErrorCode errorCode, String messageKey, Object... messageArgs) {
        super(errorCode, messageKey, messageArgs);
    }

    public PermissionException withDetail(Object detail) {
        detail(detail);
        return this;
    }
}

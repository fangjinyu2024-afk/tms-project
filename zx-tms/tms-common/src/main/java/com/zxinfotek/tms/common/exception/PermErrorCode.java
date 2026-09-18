package com.zxinfotek.tms.common.exception;

/**
 * 权限与数据范围错误码，由基础设施拦截与各业务模块共同使用。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
public enum PermErrorCode implements ErrorCode {

    PERM_001("PERM_001", "无此操作权限"),
    PERM_002("PERM_002", "超出数据范围"),
    PERM_003("PERM_003", "包含无权授予的权限项"),
    PERM_004("PERM_004", "该功能未对客户开通");

    private final String code;
    private final String message;

    PermErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}

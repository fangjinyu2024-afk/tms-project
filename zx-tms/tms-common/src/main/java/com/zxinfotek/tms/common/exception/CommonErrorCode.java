package com.zxinfotek.tms.common.exception;

public enum CommonErrorCode implements ErrorCode {

    COMMON_001("COMMON_001", "参数校验失败"),
    COMMON_002("COMMON_002", "数据已被其他人修改，请刷新后重试"),
    COMMON_003("COMMON_003", "资源不存在"),
    COMMON_004("COMMON_004", "依赖服务不可用"),
    COMMON_005("COMMON_005", "系统内部错误");

    private final String code;
    private final String message;

    CommonErrorCode(String code, String message) {
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

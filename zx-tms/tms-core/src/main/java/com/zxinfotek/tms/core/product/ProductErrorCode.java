package com.zxinfotek.tms.core.product;

import com.zxinfotek.tms.common.exception.ErrorCode;

/** product 域错误码，取值见详细设计 9.2。 */
public enum ProductErrorCode implements ErrorCode {

    PRODUCT_001("PRODUCT_001", "型号标识已存在"),
    PRODUCT_002("PRODUCT_002", "型号已被引用，不能移除");

    private final String code;
    private final String message;

    ProductErrorCode(String code, String message) {
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

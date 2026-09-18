package com.zxinfotek.tms.common.enums;

/** 登录结果，详细设计 6.2.12。 */
public enum LoginResult {

    SUCCESS("成功"),
    FAIL("失败");

    private final String label;

    LoginResult(String label) {
        this.label = label;
    }

    public String getCode() {
        return name();
    }

    public String getLabel() {
        return label;
    }
}

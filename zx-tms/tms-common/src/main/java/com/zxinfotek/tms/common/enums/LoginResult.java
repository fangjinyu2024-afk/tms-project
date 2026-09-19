package com.zxinfotek.tms.common.enums;

/** 登录结果，详细设计 6.2.12。 */
public enum LoginResult implements Labeled {

    SUCCESS("成功"),
    FAIL("失败");

    private final String label;

    LoginResult(String label) {
        this.label = label;
    }

    @Override
    public String getCode() {
        return name();
    }

    @Override
    public String getLabel() {
        return label;
    }
}

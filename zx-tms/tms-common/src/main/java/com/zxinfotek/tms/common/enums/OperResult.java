package com.zxinfotek.tms.common.enums;

/** 操作结果，详细设计 6.2.13。 */
public enum OperResult {

    SUCCESS("成功"),
    PARTIAL("部分成功"),
    FAIL("失败");

    private final String label;

    OperResult(String label) {
        this.label = label;
    }

    public String getCode() {
        return name();
    }

    public String getLabel() {
        return label;
    }
}

package com.zxinfotek.tms.common.enums;

/** 操作结果，详细设计 6.2.13。 */
public enum OperResult implements Labeled {

    SUCCESS("成功"),
    PARTIAL("部分成功"),
    FAIL("失败");

    private final String label;

    OperResult(String label) {
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

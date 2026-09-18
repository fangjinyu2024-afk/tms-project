package com.zxinfotek.tms.common.enums;

/** 会话状态，详细设计 6.2.4。 */
public enum SessionStatus {

    ACTIVE("有效"),
    INVALID("已失效");

    private final String label;

    SessionStatus(String label) {
        this.label = label;
    }

    public String getCode() {
        return name();
    }

    public String getLabel() {
        return label;
    }
}

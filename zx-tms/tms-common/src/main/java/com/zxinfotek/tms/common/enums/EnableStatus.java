package com.zxinfotek.tms.common.enums;

/** 通用启停状态，详细设计 6.2.6。 */
public enum EnableStatus {

    ENABLED("启用"),
    DISABLED("停用");

    private final String label;

    EnableStatus(String label) {
        this.label = label;
    }

    public String getCode() {
        return name();
    }

    public String getLabel() {
        return label;
    }
}

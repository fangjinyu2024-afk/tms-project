package com.zxinfotek.tms.common.enums;

/** 会话入口，详细设计 6.2.11。 */
public enum SessionEntry implements Labeled {

    CONSOLE("管理后台"),
    TOOL("激活工具");

    private final String label;

    SessionEntry(String label) {
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

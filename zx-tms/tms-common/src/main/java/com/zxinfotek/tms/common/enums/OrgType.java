package com.zxinfotek.tms.common.enums;

/** 机构类型，详细设计 6.2.3。 */
public enum OrgType {

    PLATFORM("平台"),
    TENANT_ROOT("客户根机构"),
    BRANCH("下级机构");

    private final String label;

    OrgType(String label) {
        this.label = label;
    }

    public String getCode() {
        return name();
    }

    public String getLabel() {
        return label;
    }
}

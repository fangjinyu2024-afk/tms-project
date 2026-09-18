package com.zxinfotek.tms.common.enums;

/** 产品类别，详细设计 6.2.9。 */
public enum ProductCategory {

    TRADITIONAL_POS("传统 POS"),
    DESKTOP_POS("台式 POS");

    private final String label;

    ProductCategory(String label) {
        this.label = label;
    }

    public String getCode() {
        return name();
    }

    public String getLabel() {
        return label;
    }
}

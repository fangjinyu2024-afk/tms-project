package com.zxinfotek.tms.common.enums;

/** 数据范围，详细设计 6.2.10。 */
public enum DataScope {

    SELF_ORG("仅成员所属机构", 1),
    ORG_AND_SUB("成员所属机构及下级机构", 2);

    private final String label;
    private final int rank;

    DataScope(String label, int rank) {
        this.label = label;
        this.rank = rank;
    }

    /**
     * 取范围较大的一个，用于同一权限码在多个角色间合并（详细设计 7.1）。
     *
     * @author zxinfotek
     * @since 2026-09-18
     */
    public static DataScope max(DataScope one, DataScope other) {
        if (one == null) {
            return other;
        }
        if (other == null) {
            return one;
        }
        return one.rank >= other.rank ? one : other;
    }

    public String getCode() {
        return name();
    }

    public String getLabel() {
        return label;
    }

    public int getRank() {
        return rank;
    }
}

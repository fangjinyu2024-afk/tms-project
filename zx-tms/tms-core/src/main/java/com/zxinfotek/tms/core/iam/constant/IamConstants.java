package com.zxinfotek.tms.core.iam.constant;

/** 平台固定数据：平台是租户编号 0 的客户，根机构名称恒为「平台」（详细设计 3.3.5 第 4 条）。 */
public final class IamConstants {

    public static final long PLATFORM_TENANT_ID = 0L;
    public static final long PLATFORM_ROOT_ORG_ID = 1L;
    public static final String PLATFORM_ORG_PATH = "/1/";
    public static final String PLATFORM_NAME = "平台";

    private IamConstants() {
    }
}

package com.zxinfotek.tms.common.enums;

/** 业务模块编码，详细设计 6.2.7。 */
public enum LogModule implements Labeled {

    DEVICE("设备管理"),
    DEVICE_GROUP("设备分组"),
    DEVICE_FLOW("设备流转"),
    MODE_TASK("设备模式任务"),
    PACKAGE("升级包管理"),
    OTA_TASK("OTA 任务"),
    KEY("密钥管理"),
    RKI_TASK("RKI 任务"),
    ACTIVATION_GRANT("激活授权"),
    AUTH_CODE("授权码"),
    ACTIVATION("设备激活"),
    CA("CA 管理"),
    DEVICE_CERT("设备证书"),
    SERVER_CERT("平台服务证书"),
    TENANT("客户管理"),
    ORG("机构管理"),
    MEMBER("成员管理"),
    ROLE("角色管理"),
    PRODUCT("产品与型号"),
    SESSION("在线会话"),
    AUTH("认证与账号安全");

    private final String label;

    LogModule(String label) {
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

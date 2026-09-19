package com.zxinfotek.tms.common.enums;

/** 操作类型，详细设计 6.2.8。 */
public enum OperAction implements Labeled {

    CREATE("新增"),
    UPDATE("编辑"),
    DELETE("删除"),
    IMPORT("导入"),
    EXPORT("导出"),
    PUBLISH("发布"),
    STOP("停止"),
    TOGGLE("启停"),
    AUTHORIZE("功能授权"),
    ASSIGN_ROLE("角色分配"),
    RESET_PASSWORD("重置密码"),
    FORCE_LOGOUT("强制下线"),
    REVOKE("撤销"),
    GENERATE("生成"),
    SIGN("签发"),
    PROVISION("标记产线预置"),
    VIEW_SENSITIVE("敏感信息查看");

    private final String label;

    OperAction(String label) {
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

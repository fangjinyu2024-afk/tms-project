package com.zxinfotek.tms.common.enums;

/** 会话失效原因，详细设计 6.2.5。 */
public enum SessionInvalidReason implements Labeled {

    LOGOUT("主动退出"),
    FORCE_LOGOUT("强制下线"),
    TIMEOUT("超时"),
    ACCOUNT_DISABLED("账号停用"),
    ORG_DISABLED("组织停用"),
    PASSWORD_RESET("管理员重置密码"),
    PASSWORD_CHANGED("本人修改密码"),
    MEMBER_DELETED("成员删除");

    private final String label;

    SessionInvalidReason(String label) {
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

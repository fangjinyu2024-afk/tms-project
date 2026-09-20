package com.zxinfotek.tms.core.iam;

import com.zxinfotek.tms.common.exception.ErrorCode;

/** iam 域错误码，取值见详细设计 9.2。 */
public enum IamErrorCode implements ErrorCode {

    AUTH_001("AUTH_001", "账号或密码错误"),
    AUTH_002("AUTH_002", "账号已停用"),
    AUTH_003("AUTH_003", "账号已锁定"),
    AUTH_004("AUTH_004", "登录已失效，请重新登录"),
    AUTH_005("AUTH_005", "需要先修改初始密码"),
    AUTH_006("AUTH_006", "原密码错误"),
    AUTH_007("AUTH_007", "邮箱未验证，无法使用邮件找回"),
    AUTH_008("AUTH_008", "验证码错误或已过期"),

    TENANT_001("TENANT_001", "客户名称已存在"),
    TENANT_002("TENANT_002", "型号仍有归属设备，不能取消关联"),
    TENANT_003("TENANT_003", "存在设备、任务或下级机构，不能删除"),

    ORG_001("ORG_001", "同级机构名称已存在"),
    ORG_002("ORG_002", "上级机构不可用或超出可管理范围"),
    ORG_003("ORG_003", "存在成员、设备、任务或下级机构，不能删除"),
    ORG_004("ORG_004", "根机构不可停用或删除"),

    MEMBER_001("MEMBER_001", "账号已存在"),
    MEMBER_002("MEMBER_002", "账号格式不合法"),
    MEMBER_003("MEMBER_003", "邮箱已被绑定"),
    MEMBER_004("MEMBER_004", "至少选择一个角色"),
    MEMBER_005("MEMBER_005", "不能移除本机构最后一名管理员的管理权限"),

    ROLE_001("ROLE_001", "角色名称已存在"),
    ROLE_002("ROLE_002", "内置角色不可修改"),
    ROLE_003("ROLE_003", "角色已被成员引用，不能删除"),

    SESSION_001("SESSION_001", "会话已失效"),
    SESSION_002("SESSION_002", "请填写下线原因"),
    SESSION_003("SESSION_003", "不能强制下线当前会话");

    private final String code;
    private final String message;

    IamErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}

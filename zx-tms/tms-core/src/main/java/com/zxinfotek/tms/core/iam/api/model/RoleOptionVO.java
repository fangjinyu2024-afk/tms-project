package com.zxinfotek.tms.core.iam.api.model;

import lombok.Data;

@Data
public class RoleOptionVO {

    private Long id;
    private String name;
    private String ownerOrgName;
    private String dataScope;
    private Boolean builtin;
    /** 操作者是否有权分配该角色，无权时页面只读展示 */
    private Boolean assignable;
}

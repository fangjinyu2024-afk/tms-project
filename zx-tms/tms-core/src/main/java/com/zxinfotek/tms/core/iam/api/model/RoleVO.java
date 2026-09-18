package com.zxinfotek.tms.core.iam.api.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RoleVO {

    private Long id;
    private Long tenantId;
    private String name;
    private String description;
    private Long ownerOrgId;
    private String ownerOrgName;
    private String dataScope;
    private String dataScopeLabel;
    private Boolean builtin;
    private String status;
    private Integer version;
    private Integer memberCount;
    private List<String> permCodes;
    private LocalDateTime createTime;
}

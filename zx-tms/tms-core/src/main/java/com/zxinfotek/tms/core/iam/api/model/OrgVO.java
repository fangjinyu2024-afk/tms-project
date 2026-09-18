package com.zxinfotek.tms.core.iam.api.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrgVO {

    private Long id;
    private Long tenantId;
    private String tenantName;
    private Long parentId;
    private String parentName;
    private String orgPath;
    private String orgType;
    private String orgTypeLabel;
    private String name;
    private String contactName;
    private String contactPhone;
    private String status;
    private Integer memberCount;
    private Integer subOrgCount;
    private LocalDateTime createTime;
}

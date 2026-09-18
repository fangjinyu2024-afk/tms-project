package com.zxinfotek.tms.core.iam.api.model;

import lombok.Data;

@Data
public class TenantCreateResultVO {

    private Long tenantId;
    private Long rootOrgId;
    private Long adminMemberId;
    private String adminAccount;
    /** 系统生成的初始密码，只在本次响应返回一次 */
    private String initialPassword;
}

package com.zxinfotek.tms.core.iam.api.model;

import lombok.Data;

@Data
public class MemberProfileVO {

    private Long memberId;
    private String account;
    private String nickname;
    private String email;
    private Boolean emailVerified;
    private String phone;
    private Long tenantId;
    private String tenantName;
    private Long orgId;
    private String orgName;
    private String orgPath;
    private Boolean platform;
}

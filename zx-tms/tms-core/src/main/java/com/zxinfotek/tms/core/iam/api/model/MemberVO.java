package com.zxinfotek.tms.core.iam.api.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MemberVO {

    private Long id;
    private Long tenantId;
    private String tenantName;
    private Long orgId;
    private String orgName;
    private String orgPath;
    private String account;
    private String nickname;
    private String email;
    private Boolean emailVerified;
    private String phone;
    private String status;
    private Boolean mustChangePassword;
    private LocalDateTime lastPasswordTime;
    private List<RoleOptionVO> roles;
    private LocalDateTime createTime;
}

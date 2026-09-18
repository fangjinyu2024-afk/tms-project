package com.zxinfotek.tms.core.audit.api.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LoginLogVO {

    private Long id;
    private String account;
    private Long memberId;
    private Long orgId;
    private String orgName;
    private String entry;
    private String entryLabel;
    private String result;
    private String resultLabel;
    private String failReason;
    private String clientIp;
    private String userAgent;
    private LocalDateTime loginTime;
}

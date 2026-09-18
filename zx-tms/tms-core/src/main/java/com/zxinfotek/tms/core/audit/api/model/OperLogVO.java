package com.zxinfotek.tms.core.audit.api.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OperLogVO {

    private Long id;
    private String module;
    private String moduleLabel;
    private String action;
    private String actionLabel;
    private String objectType;
    private String objectId;
    private String objectName;
    private String result;
    private String resultLabel;
    private String failReason;
    private Integer totalCount;
    private Integer successCount;
    private String changeSummary;
    private Long memberId;
    private String account;
    private String nickname;
    private Long orgId;
    private String orgName;
    private String clientIp;
    private String traceId;
    private LocalDateTime operTime;
}

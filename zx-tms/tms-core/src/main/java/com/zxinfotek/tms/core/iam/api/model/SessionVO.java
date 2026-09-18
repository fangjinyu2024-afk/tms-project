package com.zxinfotek.tms.core.iam.api.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SessionVO {

    private Long id;
    private Long memberId;
    private String account;
    private String nickname;
    private Long orgId;
    private String orgName;
    private String entry;
    private String entryLabel;
    private String clientIp;
    private String userAgent;
    private LocalDateTime loginTime;
    private LocalDateTime lastActiveTime;
    /** 是否为发起查询的这条会话，页面标注「当前会话」且不提供强制下线入口 */
    private Boolean current;
}

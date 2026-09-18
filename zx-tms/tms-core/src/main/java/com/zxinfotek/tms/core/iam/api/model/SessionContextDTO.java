package com.zxinfotek.tms.core.iam.api.model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 登录会话上下文，存放于 Redis，鉴权时据此还原请求上下文并比对权限版本（详细设计 7.2）。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
@Data
public class SessionContextDTO implements Serializable {

    private Long sessionId;
    private String tokenHash;
    private Long memberId;
    private String account;
    private String nickname;
    private Long tenantId;
    private Long orgId;
    private String orgPath;
    private String entry;
    private String clientIp;
    private String userAgent;
    private boolean mustChangePassword;
    private LocalDateTime loginTime;
    private LocalDateTime expireTime;
    private LocalDateTime lastActiveTime;
    private Integer featureVersion;
    private Integer memberPermVersion;
    private Integer rolePermVersion;
    private Map<String, String> permissions = new LinkedHashMap<>();
}

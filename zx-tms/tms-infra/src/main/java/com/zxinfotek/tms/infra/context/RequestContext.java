package com.zxinfotek.tms.infra.context;

import com.zxinfotek.tms.common.enums.DataScope;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 单次请求的上下文，承载登录主体、有效权限与本次操作命中的数据范围。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
public class RequestContext implements Serializable {

    /** 平台租户编号，详细设计 4.3.1 */
    public static final long PLATFORM_TENANT_ID = 0L;

    private String traceId;
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
    private Map<String, DataScope> permissions = new LinkedHashMap<>();
    private DataScope dataScope;

    public boolean isAuthenticated() {
        return memberId != null;
    }

    public boolean isPlatform() {
        return tenantId != null && tenantId == PLATFORM_TENANT_ID;
    }

    public boolean hasPermission(String permCode) {
        return permissions.containsKey(permCode);
    }

    public DataScope scopeOf(String permCode) {
        return permissions.get(permCode);
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public String getOrgPath() {
        return orgPath;
    }

    public void setOrgPath(String orgPath) {
        this.orgPath = orgPath;
    }

    public String getEntry() {
        return entry;
    }

    public void setEntry(String entry) {
        this.entry = entry;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }

    public Map<String, DataScope> getPermissions() {
        return permissions == null ? Collections.emptyMap() : permissions;
    }

    public void setPermissions(Map<String, DataScope> permissions) {
        this.permissions = permissions == null ? new LinkedHashMap<>() : permissions;
    }

    public DataScope getDataScope() {
        return dataScope;
    }

    public void setDataScope(DataScope dataScope) {
        this.dataScope = dataScope;
    }
}

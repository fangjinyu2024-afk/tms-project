package com.zxinfotek.tms.admin.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zxinfotek.tms.common.enums.DataScope;
import com.zxinfotek.tms.common.exception.CommonErrorCode;
import com.zxinfotek.tms.common.exception.ErrorCode;
import com.zxinfotek.tms.common.model.Result;
import com.zxinfotek.tms.core.iam.IamErrorCode;
import com.zxinfotek.tms.core.iam.api.LoginSessionService;
import com.zxinfotek.tms.core.iam.api.PermissionService;
import com.zxinfotek.tms.core.iam.api.model.SessionContextDTO;
import com.zxinfotek.tms.infra.context.RequestContext;
import com.zxinfotek.tms.infra.context.RequestContextHolder;
import com.zxinfotek.tms.infra.i18n.I18nMessages;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 请求鉴权：按令牌还原会话、比对权限版本、必要时重新加载有效权限，并拦截未完成初始改密的请求。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
@Component
@Order(2)
public class AuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AuthFilter.class);

    private static final String BEARER = "Bearer ";

    /** 无需认证的路径 */
    private static final List<String> ANONYMOUS_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/password/forgot",
            "/api/auth/password/reset");

    /** 强制改密期间仍可访问的路径 */
    private static final List<String> PASSWORD_CHANGE_PATHS = List.of(
            "/api/auth/password",
            "/api/auth/logout",
            "/api/auth/profile");

    private final LoginSessionService loginSessionService;
    private final PermissionService permissionService;
    private final ObjectMapper objectMapper;

    public AuthFilter(LoginSessionService loginSessionService,
                      PermissionService permissionService,
                      ObjectMapper objectMapper) {
        this.loginSessionService = loginSessionService;
        this.permissionService = permissionService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/") || ANONYMOUS_PATHS.contains(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith(BEARER)) {
            reject(response, HttpStatus.UNAUTHORIZED, IamErrorCode.AUTH_004);
            return;
        }
        SessionContextDTO session;
        try {
            session = loginSessionService.authenticate(header.substring(BEARER.length()).trim());
            if (session == null) {
                reject(response, HttpStatus.UNAUTHORIZED, IamErrorCode.AUTH_004);
                return;
            }
            if (!loginSessionService.refresh(session)) {
                reject(response, HttpStatus.UNAUTHORIZED, IamErrorCode.AUTH_004);
                return;
            }
            reloadPermissionsIfChanged(session);
        } catch (DataAccessException e) {
            // Redis 或数据库不可用时鉴权直接失败，不降级为放行
            log.error("鉴权依赖不可用", e);
            reject(response, HttpStatus.SERVICE_UNAVAILABLE, CommonErrorCode.COMMON_004);
            return;
        }

        applyContext(session);
        if (session.isMustChangePassword() && !PASSWORD_CHANGE_PATHS.contains(request.getRequestURI())) {
            reject(response, HttpStatus.BAD_REQUEST, IamErrorCode.AUTH_005);
            return;
        }
        chain.doFilter(request, response);
    }

    /** 权限版本不一致即重新加载有效权限并刷新会话，不等待会话过期（详细设计 7.2）。 */
    private void reloadPermissionsIfChanged(SessionContextDTO session) {
        boolean changed = permissionService.versionChanged(session.getMemberId(),
                session.getFeatureVersion(), session.getMemberPermVersion(), session.getRolePermVersion());
        if (!changed) {
            return;
        }
        PermissionService.EffectivePermissions effective = permissionService.load(session.getMemberId());
        Map<String, String> permissions = new LinkedHashMap<>();
        effective.permissions().forEach((permCode, scope) ->
                permissions.put(permCode, scope == null ? DataScope.SELF_ORG.getCode() : scope.getCode()));
        session.setPermissions(permissions);
        session.setFeatureVersion(effective.featureVersion());
        session.setMemberPermVersion(effective.memberPermVersion());
        session.setRolePermVersion(effective.rolePermVersion());
        loginSessionService.updateSessionPermissions(session);
    }

    private void applyContext(SessionContextDTO session) {
        RequestContext context = RequestContextHolder.get();
        context.setSessionId(session.getSessionId());
        context.setTokenHash(session.getTokenHash());
        context.setMemberId(session.getMemberId());
        context.setAccount(session.getAccount());
        context.setNickname(session.getNickname());
        context.setTenantId(session.getTenantId());
        context.setOrgId(session.getOrgId());
        context.setOrgPath(session.getOrgPath());
        context.setEntry(session.getEntry());
        context.setMustChangePassword(session.isMustChangePassword());
        Map<String, DataScope> permissions = new LinkedHashMap<>();
        session.getPermissions().forEach((permCode, scope) ->
                permissions.put(permCode, DataScope.valueOf(scope)));
        context.setPermissions(permissions);
    }

    private void reject(HttpServletResponse response, HttpStatus status, ErrorCode errorCode) throws IOException {
        response.setStatus(status.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("X-Biz-Code", errorCode.getCode());
        Result<Void> result = Result.fail(errorCode,
                I18nMessages.getOrDefault(errorCode.getMessageKey(), errorCode.getMessage()));
        result.setTraceId(RequestContextHolder.get().getTraceId());
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}

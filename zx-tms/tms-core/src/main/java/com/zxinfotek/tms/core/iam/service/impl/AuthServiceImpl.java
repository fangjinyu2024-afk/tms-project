package com.zxinfotek.tms.core.iam.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zxinfotek.tms.common.enums.DataScope;
import com.zxinfotek.tms.common.enums.EnableStatus;
import com.zxinfotek.tms.common.enums.LoginResult;
import com.zxinfotek.tms.common.enums.SessionInvalidReason;
import com.zxinfotek.tms.common.exception.AuthException;
import com.zxinfotek.tms.common.exception.BizException;
import com.zxinfotek.tms.common.exception.NotFoundException;
import com.zxinfotek.tms.common.util.IdUtils;
import com.zxinfotek.tms.common.util.UtcTimes;
import com.zxinfotek.tms.core.audit.api.LoginLogService;
import com.zxinfotek.tms.core.audit.api.model.LoginLogRecord;
import com.zxinfotek.tms.core.iam.IamErrorCode;
import com.zxinfotek.tms.core.iam.api.AuthService;
import com.zxinfotek.tms.core.iam.api.LoginSessionService;
import com.zxinfotek.tms.core.iam.api.PermissionService;
import com.zxinfotek.tms.core.iam.api.model.CaptchaVO;
import com.zxinfotek.tms.core.iam.api.model.ChangePasswordRequest;
import com.zxinfotek.tms.core.iam.api.model.EmailVerifyRequest;
import com.zxinfotek.tms.core.iam.api.model.ForgotPasswordRequest;
import com.zxinfotek.tms.core.iam.api.model.LoginRequest;
import com.zxinfotek.tms.core.iam.api.model.LoginTokenDTO;
import com.zxinfotek.tms.core.iam.api.model.LoginVO;
import com.zxinfotek.tms.core.iam.api.model.MemberProfileVO;
import com.zxinfotek.tms.core.iam.api.model.ProfileVO;
import com.zxinfotek.tms.core.iam.api.model.ResetPasswordRequest;
import com.zxinfotek.tms.core.iam.api.model.SessionContextDTO;
import com.zxinfotek.tms.core.iam.api.model.UpdateProfileRequest;
import com.zxinfotek.tms.core.iam.config.SecurityProperties;
import com.zxinfotek.tms.core.iam.constant.IamConstants;
import com.zxinfotek.tms.core.iam.entity.MemberEntity;
import com.zxinfotek.tms.core.iam.entity.OrgEntity;
import com.zxinfotek.tms.core.iam.entity.TenantEntity;
import com.zxinfotek.tms.core.iam.mapper.MemberMapper;
import com.zxinfotek.tms.core.iam.mapper.OrgMapper;
import com.zxinfotek.tms.core.iam.mapper.TenantMapper;
import com.zxinfotek.tms.infra.captcha.CaptchaService;
import com.zxinfotek.tms.infra.context.RequestContext;
import com.zxinfotek.tms.infra.context.RequestContextHolder;
import com.zxinfotek.tms.infra.i18n.I18nMessages;
import com.zxinfotek.tms.infra.mail.MailService;
import com.zxinfotek.tms.infra.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * 登录认证与账号自助操作：登录校验、会话签发、密码与邮箱维护（详细设计 3.1）。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private static final String RESET_TOKEN_PREFIX = "tms:pwd-reset:";
    private static final String EMAIL_TOKEN_PREFIX = "tms:email-verify:";

    private final MemberMapper memberMapper;
    private final OrgMapper orgMapper;
    private final TenantMapper tenantMapper;
    private final PermissionService permissionService;
    private final LoginSessionService loginSessionService;
    private final LoginLogService loginLogService;
    private final PasswordEncoder passwordEncoder;
    private final SecurityProperties securityProperties;
    private final RedisService redisService;
    private final MailService mailService;
    private final CaptchaService captchaService;

    public AuthServiceImpl(MemberMapper memberMapper,
                           OrgMapper orgMapper,
                           TenantMapper tenantMapper,
                           PermissionService permissionService,
                           LoginSessionService loginSessionService,
                           LoginLogService loginLogService,
                           PasswordEncoder passwordEncoder,
                           SecurityProperties securityProperties,
                           RedisService redisService,
                           MailService mailService,
                           CaptchaService captchaService) {
        this.memberMapper = memberMapper;
        this.orgMapper = orgMapper;
        this.tenantMapper = tenantMapper;
        this.permissionService = permissionService;
        this.loginSessionService = loginSessionService;
        this.loginLogService = loginLogService;
        this.passwordEncoder = passwordEncoder;
        this.securityProperties = securityProperties;
        this.redisService = redisService;
        this.mailService = mailService;
        this.captchaService = captchaService;
    }

    @Override
    public CaptchaVO createCaptcha() {
        CaptchaService.CaptchaImage image = captchaService.create();
        CaptchaVO vo = new CaptchaVO();
        vo.setCaptchaId(image.id());
        vo.setImage(image.image());
        return vo;
    }

    /**
     * 登录：按账号、状态、锁定与密码依次校验，通过后加载有效权限并签发会话，成功与失败均写登录日志。
     *
     * @author zxinfotek
     * @since 2026-09-18
     */
    @Override
    public LoginVO login(LoginRequest request) {
        // 验证码先于账号与密码校验，且失败不累加密码失败次数，避免被用来锁定他人账号（详细设计 3.1.5 第 13 条）
        if (!captchaService.verify(request.getCaptchaId(), request.getCaptchaCode())) {
            writeLoginLog(null, request, LoginResult.FAIL, "msg.login.captchaInvalid");
            throw new BizException(IamErrorCode.AUTH_008);
        }
        String accountLower = request.getAccount().trim().toLowerCase();
        MemberEntity member = memberMapper.selectOne(Wrappers.<MemberEntity>lambdaQuery()
                .eq(MemberEntity::getAccountLower, accountLower).last("LIMIT 1"));
        if (member == null) {
            writeLoginLog(null, request, LoginResult.FAIL, "msg.login.badCredentials");
            throw new BizException(IamErrorCode.AUTH_001);
        }
        LocalDateTime now = UtcTimes.now();
        if (member.getLockedUntil() != null && member.getLockedUntil().isAfter(now)) {
            long minutes = Math.max(1, Duration.between(now, member.getLockedUntil()).toMinutes());
            writeLoginLog(member, request, LoginResult.FAIL, "msg.login.locked");
            throw new BizException(IamErrorCode.AUTH_003, "msg.login.lockedRetry", minutes);
        }
        if (!organizationEnabled(member)) {
            writeLoginLog(member, request, LoginResult.FAIL, "msg.login.disabled");
            loginSessionService.invalidateByMember(member.getId(), SessionInvalidReason.ACCOUNT_DISABLED);
            throw new BizException(IamErrorCode.AUTH_002);
        }
        if (!passwordEncoder.matches(request.getPassword(), member.getPasswordHash())) {
            int failCount = (member.getFailCount() == null ? 0 : member.getFailCount()) + 1;
            member.setFailCount(failCount);
            if (failCount >= securityProperties.getLoginFailThreshold()) {
                member.setLockedUntil(now.plus(securityProperties.getLockDuration()));
                member.setFailCount(0);
            }
            memberMapper.updateById(member);
            writeLoginLog(member, request, LoginResult.FAIL, "msg.login.badCredentials");
            throw new BizException(IamErrorCode.AUTH_001);
        }

        member.setFailCount(0);
        member.setLockedUntil(null);
        memberMapper.updateById(member);

        PermissionService.EffectivePermissions effective = permissionService.load(member.getId());
        SessionContextDTO context = buildSessionContext(member, request, effective);
        LoginTokenDTO token = loginSessionService.createSession(context);
        writeLoginLog(member, request, LoginResult.SUCCESS, null);

        LoginVO vo = new LoginVO();
        vo.setToken(token.getToken());
        vo.setExpiresIn(token.getExpiresIn());
        vo.setMustChangePassword(isTrue(member.getMustChangePassword()));
        vo.setMember(toProfile(member));
        vo.setPermissions(context.getPermissions());
        vo.setMenus(menusOf(context.getPermissions().keySet()));
        return vo;
    }

    @Override
    public void logout() {
        loginSessionService.invalidateCurrent(SessionInvalidReason.LOGOUT);
    }

    @Override
    public ProfileVO currentProfile() {
        MemberEntity member = currentMember();
        RequestContext context = RequestContextHolder.get();
        Map<String, String> permissions = new LinkedHashMap<>();
        context.getPermissions().forEach((permCode, scope) ->
                permissions.put(permCode, scope == null ? DataScope.SELF_ORG.getCode() : scope.getCode()));

        ProfileVO vo = new ProfileVO();
        vo.setMember(toProfile(member));
        vo.setPermissions(permissions);
        vo.setMenus(menusOf(permissions.keySet()));
        vo.setMustChangePassword(isTrue(member.getMustChangePassword()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(UpdateProfileRequest request) {
        MemberEntity member = currentMember();
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            Long exists = memberMapper.selectCount(Wrappers.<MemberEntity>lambdaQuery()
                    .eq(MemberEntity::getEmail, request.getEmail().trim())
                    .ne(MemberEntity::getId, member.getId()));
            if (exists != null && exists > 0) {
                throw new BizException(IamErrorCode.MEMBER_003);
            }
        }
        boolean emailChanged = request.getEmail() != null
                && !request.getEmail().equalsIgnoreCase(member.getEmail());
        member.setNickname(request.getNickname());
        member.setEmail(request.getEmail() == null || request.getEmail().isBlank()
                ? null : request.getEmail().trim());
        member.setPhone(request.getPhone());
        if (emailChanged) {
            member.setEmailVerified(0);
        }
        memberMapper.updateById(member);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(ChangePasswordRequest request) {
        MemberEntity member = currentMember();
        if (!passwordEncoder.matches(request.getOldPassword(), member.getPasswordHash())) {
            throw new BizException(IamErrorCode.AUTH_006);
        }
        applyNewPassword(member, request.getNewPassword(), SessionInvalidReason.PASSWORD_CHANGED);
    }

    /** 找回密码仅对已验证邮箱的账号可用；无邮箱成员由有权限的管理员重置密码。 */
    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        if (!captchaService.verify(request.getCaptchaId(), request.getCaptchaCode())) {
            throw new BizException(IamErrorCode.AUTH_008);
        }
        String accountLower = request.getAccount().trim().toLowerCase();
        MemberEntity member = memberMapper.selectOne(Wrappers.<MemberEntity>lambdaQuery()
                .eq(MemberEntity::getAccountLower, accountLower).last("LIMIT 1"));
        if (member == null) {
            // 不暴露账号是否存在
            log.info("找回密码请求的账号不存在");
            return;
        }
        if (member.getEmail() == null || !isTrue(member.getEmailVerified())) {
            throw new BizException(IamErrorCode.AUTH_007);
        }
        String token = IdUtils.randomHex(16);
        redisService.set(RESET_TOKEN_PREFIX + token, member.getId(), securityProperties.getResetTokenExpire());
        mailService.send(member.getEmail(), I18nMessages.get("mail.password.subject"),
                I18nMessages.get("mail.password.body", securityProperties.getResetTokenExpire().toMinutes(),
                        securityProperties.getConsoleBaseUrl() + "/reset-password?token=" + token));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPasswordByToken(ResetPasswordRequest request) {
        Object memberId = redisService.get(RESET_TOKEN_PREFIX + request.getToken(), Object.class);
        if (memberId == null) {
            throw new AuthException(IamErrorCode.AUTH_004, "msg.password.resetTokenInvalid");
        }
        MemberEntity member = memberMapper.selectById(Long.valueOf(String.valueOf(memberId)));
        if (member == null) {
            throw new NotFoundException("msg.member.notFound");
        }
        redisService.delete(RESET_TOKEN_PREFIX + request.getToken());
        applyNewPassword(member, request.getNewPassword(), SessionInvalidReason.PASSWORD_CHANGED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void emailVerify(EmailVerifyRequest request) {
        if ("confirm".equalsIgnoreCase(request.getAction())) {
            Object memberId = redisService.get(EMAIL_TOKEN_PREFIX + request.getToken(), Object.class);
            if (memberId == null) {
                throw new BizException(IamErrorCode.AUTH_007, "msg.email.tokenInvalid");
            }
            MemberEntity member = memberMapper.selectById(Long.valueOf(String.valueOf(memberId)));
            if (member == null) {
                throw new NotFoundException("msg.member.notFound");
            }
            member.setEmailVerified(1);
            memberMapper.updateById(member);
            redisService.delete(EMAIL_TOKEN_PREFIX + request.getToken());
            return;
        }
        MemberEntity member = currentMember();
        if (member.getEmail() == null || member.getEmail().isBlank()) {
            throw new BizException(IamErrorCode.AUTH_007, "msg.email.required");
        }
        String token = IdUtils.randomHex(16);
        redisService.set(EMAIL_TOKEN_PREFIX + token, member.getId(), securityProperties.getEmailTokenExpire());
        mailService.send(member.getEmail(), I18nMessages.get("mail.email.subject"),
                I18nMessages.get("mail.email.body",
                        securityProperties.getConsoleBaseUrl() + "/verify-email?token=" + token));
    }

    private void applyNewPassword(MemberEntity member, String newPassword, SessionInvalidReason reason) {
        member.setPasswordHash(passwordEncoder.encode(newPassword));
        member.setMustChangePassword(0);
        member.setPasswordUpdatedAt(UtcTimes.now());
        member.setFailCount(0);
        member.setLockedUntil(null);
        memberMapper.updateById(member);
        loginSessionService.invalidateByMember(member.getId(), reason);
    }

    private SessionContextDTO buildSessionContext(MemberEntity member, LoginRequest request,
                                                  PermissionService.EffectivePermissions effective) {
        RequestContext requestContext = RequestContextHolder.get();
        SessionContextDTO context = new SessionContextDTO();
        context.setMemberId(member.getId());
        context.setAccount(member.getAccount());
        context.setNickname(member.getNickname());
        context.setTenantId(member.getTenantId());
        context.setOrgId(member.getOrgId());
        context.setOrgPath(member.getOrgPath());
        context.setEntry(request.getEntry().name());
        context.setClientIp(requestContext.getClientIp());
        context.setUserAgent(requestContext.getUserAgent());
        context.setMustChangePassword(isTrue(member.getMustChangePassword()));
        context.setFeatureVersion(effective.featureVersion());
        context.setMemberPermVersion(effective.memberPermVersion());
        context.setRolePermVersion(effective.rolePermVersion());
        Map<String, String> permissions = new LinkedHashMap<>();
        effective.permissions().forEach((permCode, scope) ->
                permissions.put(permCode, scope == null ? DataScope.SELF_ORG.getCode() : scope.getCode()));
        context.setPermissions(permissions);
        return context;
    }

    private boolean organizationEnabled(MemberEntity member) {
        if (member.getStatus() != EnableStatus.ENABLED) {
            return false;
        }
        OrgEntity org = orgMapper.selectById(member.getOrgId());
        if (org == null || org.getStatus() != EnableStatus.ENABLED) {
            return false;
        }
        if (member.getTenantId() == IamConstants.PLATFORM_TENANT_ID) {
            return true;
        }
        TenantEntity tenant = tenantMapper.selectById(member.getTenantId());
        return tenant != null && tenant.getStatus() == EnableStatus.ENABLED;
    }

    private void writeLoginLog(MemberEntity member, LoginRequest request, LoginResult result, String failReason) {
        RequestContext context = RequestContextHolder.get();
        LoginLogRecord record = new LoginLogRecord();
        record.setAccount(request.getAccount());
        record.setEntry(request.getEntry());
        record.setResult(result);
        record.setFailReason(failReason);
        record.setClientIp(context.getClientIp());
        record.setUserAgent(context.getUserAgent());
        if (member != null) {
            record.setTenantId(member.getTenantId());
            record.setOrgId(member.getOrgId());
            record.setOrgPath(member.getOrgPath());
            record.setMemberId(member.getId());
        }
        loginLogService.record(record);
    }

    private MemberProfileVO toProfile(MemberEntity member) {
        MemberProfileVO vo = new MemberProfileVO();
        vo.setMemberId(member.getId());
        vo.setAccount(member.getAccount());
        vo.setNickname(member.getNickname());
        vo.setEmail(member.getEmail());
        vo.setEmailVerified(isTrue(member.getEmailVerified()));
        vo.setPhone(member.getPhone());
        vo.setTenantId(member.getTenantId());
        TenantEntity tenant = tenantMapper.selectById(member.getTenantId());
        vo.setTenantName(tenant == null ? null : tenant.getName());
        vo.setOrgId(member.getOrgId());
        OrgEntity org = orgMapper.selectById(member.getOrgId());
        vo.setOrgName(org == null ? null : org.getName());
        vo.setOrgPath(member.getOrgPath());
        vo.setPlatform(member.getTenantId() == IamConstants.PLATFORM_TENANT_ID);
        return vo;
    }

    private List<String> menusOf(Set<String> permCodes) {
        Set<String> menus = new TreeSet<>();
        for (String permCode : permCodes) {
            int split = permCode.indexOf(':');
            menus.add(split < 0 ? permCode : permCode.substring(0, split));
        }
        return new ArrayList<>(menus);
    }

    private MemberEntity currentMember() {
        Long memberId = RequestContextHolder.get().getMemberId();
        if (memberId == null) {
            throw new AuthException(IamErrorCode.AUTH_004);
        }
        MemberEntity member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new AuthException(IamErrorCode.AUTH_004);
        }
        return member;
    }

    private static boolean isTrue(Integer value) {
        return value != null && value == 1;
    }
}

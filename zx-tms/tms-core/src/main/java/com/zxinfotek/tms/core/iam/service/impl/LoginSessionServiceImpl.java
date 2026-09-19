package com.zxinfotek.tms.core.iam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zxinfotek.tms.common.enums.SessionEntry;
import com.zxinfotek.tms.common.enums.SessionInvalidReason;
import com.zxinfotek.tms.common.enums.SessionStatus;
import com.zxinfotek.tms.common.exception.BizException;
import com.zxinfotek.tms.common.exception.NotFoundException;
import com.zxinfotek.tms.common.model.PageResult;
import com.zxinfotek.tms.common.util.UtcTimes;
import com.zxinfotek.tms.core.iam.IamErrorCode;
import com.zxinfotek.tms.core.iam.api.LoginSessionService;
import com.zxinfotek.tms.core.iam.api.model.ForceLogoutRequest;
import com.zxinfotek.tms.core.iam.api.model.LoginTokenDTO;
import com.zxinfotek.tms.core.iam.api.model.SessionContextDTO;
import com.zxinfotek.tms.core.iam.api.model.SessionQuery;
import com.zxinfotek.tms.core.iam.api.model.SessionVO;
import com.zxinfotek.tms.core.iam.config.SessionProperties;
import com.zxinfotek.tms.core.iam.entity.LoginSessionEntity;
import com.zxinfotek.tms.core.iam.entity.OrgEntity;
import com.zxinfotek.tms.core.iam.mapper.LoginSessionMapper;
import com.zxinfotek.tms.core.iam.mapper.OrgMapper;
import com.zxinfotek.tms.infra.context.DataScopeAssert;
import com.zxinfotek.tms.infra.context.RequestContext;
import com.zxinfotek.tms.infra.context.RequestContextHolder;
import com.zxinfotek.tms.infra.excel.ExcelExportService;
import com.zxinfotek.tms.infra.i18n.I18nMessages;
import com.zxinfotek.tms.infra.redis.RedisService;
import com.zxinfotek.tms.infra.security.TokenHasher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 登录会话：Redis 承载有效性判定，数据库保留在线会话列表与失效审计（详细设计 3.1、3.7）。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
@Service
public class LoginSessionServiceImpl implements LoginSessionService {

    private static final String SESSION_KEY_PREFIX = "tms:session:";

    private final LoginSessionMapper loginSessionMapper;
    private final OrgMapper orgMapper;
    private final RedisService redisService;
    private final SessionProperties sessionProperties;
    private final ExcelExportService excelExportService;

    public LoginSessionServiceImpl(LoginSessionMapper loginSessionMapper,
                                   OrgMapper orgMapper,
                                   RedisService redisService,
                                   SessionProperties sessionProperties,
                                   ExcelExportService excelExportService) {
        this.loginSessionMapper = loginSessionMapper;
        this.orgMapper = orgMapper;
        this.redisService = redisService;
        this.sessionProperties = sessionProperties;
        this.excelExportService = excelExportService;
    }

    /**
     * 建立会话：生成 32 字节随机令牌，按 SHA-256 摘要作为 Redis 键与数据库标识，不保存明文令牌。
     *
     * @author zxinfotek
     * @since 2026-09-18
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginTokenDTO createSession(SessionContextDTO context) {
        String token = TokenHasher.newToken();
        String tokenHash = TokenHasher.hash(token);
        LocalDateTime now = UtcTimes.now();

        LoginSessionEntity entity = new LoginSessionEntity();
        entity.setTenantId(context.getTenantId());
        entity.setOrgId(context.getOrgId());
        entity.setOrgPath(context.getOrgPath());
        entity.setMemberId(context.getMemberId());
        entity.setAccount(context.getAccount());
        entity.setNickname(context.getNickname());
        entity.setTokenHash(tokenHash);
        entity.setEntry(SessionEntry.valueOf(context.getEntry()));
        entity.setClientIp(context.getClientIp());
        entity.setUserAgent(context.getUserAgent());
        entity.setLoginTime(now);
        entity.setLastActiveTime(now);
        entity.setExpireTime(now.plus(sessionProperties.getMaxLifetime()));
        entity.setStatus(SessionStatus.ACTIVE);
        loginSessionMapper.insert(entity);

        context.setSessionId(entity.getId());
        context.setTokenHash(tokenHash);
        context.setLoginTime(now);
        context.setLastActiveTime(now);
        context.setExpireTime(entity.getExpireTime());
        redisService.set(SESSION_KEY_PREFIX + tokenHash, context, sessionProperties.getIdleTimeout());

        LoginTokenDTO result = new LoginTokenDTO();
        result.setSessionId(entity.getId());
        result.setToken(token);
        result.setExpiresIn((int) sessionProperties.getIdleTimeout().getSeconds());
        return result;
    }

    @Override
    public SessionContextDTO authenticate(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        String tokenHash = TokenHasher.hash(token);
        SessionContextDTO context = redisService.get(SESSION_KEY_PREFIX + tokenHash, SessionContextDTO.class);
        if (context == null) {
            return null;
        }
        if (context.getExpireTime() != null && UtcTimes.now().isAfter(context.getExpireTime())) {
            invalidate(context.getSessionId(), tokenHash, SessionInvalidReason.TIMEOUT, null, null);
            return null;
        }
        return context;
    }

    /** 刷新滑动有效期，最近活动时间按节流间隔回写数据库，避免每请求一次更新。 */
    @Override
    public boolean refresh(SessionContextDTO context) {
        LocalDateTime now = UtcTimes.now();
        if (context.getExpireTime() != null && now.isAfter(context.getExpireTime())) {
            invalidate(context.getSessionId(), context.getTokenHash(), SessionInvalidReason.TIMEOUT, null, null);
            return false;
        }
        String key = SESSION_KEY_PREFIX + context.getTokenHash();
        LocalDateTime lastActive = context.getLastActiveTime();
        if (lastActive == null
                || Duration.between(lastActive, now).compareTo(sessionProperties.getActiveWriteBack()) >= 0) {
            context.setLastActiveTime(now);
            LoginSessionEntity entity = new LoginSessionEntity();
            entity.setId(context.getSessionId());
            entity.setLastActiveTime(now);
            loginSessionMapper.updateById(entity);
            redisService.set(key, context, sessionProperties.getIdleTimeout());
        } else {
            redisService.expire(key, sessionProperties.getIdleTimeout());
        }
        return true;
    }

    @Override
    public void updateSessionPermissions(SessionContextDTO context) {
        redisService.set(SESSION_KEY_PREFIX + context.getTokenHash(), context,
                sessionProperties.getIdleTimeout());
    }

    @Override
    public void invalidateCurrent(SessionInvalidReason reason) {
        RequestContext context = RequestContextHolder.get();
        if (context.getSessionId() == null) {
            return;
        }
        invalidate(context.getSessionId(), context.getTokenHash(), reason, null, null);
    }

    @Override
    public void invalidateByMember(Long memberId, SessionInvalidReason reason) {
        List<LoginSessionEntity> sessions = loginSessionMapper.selectList(
                Wrappers.<LoginSessionEntity>lambdaQuery()
                        .eq(LoginSessionEntity::getMemberId, memberId)
                        .eq(LoginSessionEntity::getStatus, SessionStatus.ACTIVE));
        sessions.forEach(session -> invalidate(session.getId(), session.getTokenHash(), reason, null, null));
    }

    @Override
    public void invalidateByOrgPath(Long tenantId, String orgPath, SessionInvalidReason reason) {
        List<LoginSessionEntity> sessions = loginSessionMapper.selectList(
                Wrappers.<LoginSessionEntity>lambdaQuery()
                        .eq(LoginSessionEntity::getTenantId, tenantId)
                        .likeRight(LoginSessionEntity::getOrgPath, orgPath)
                        .eq(LoginSessionEntity::getStatus, SessionStatus.ACTIVE));
        sessions.forEach(session -> invalidate(session.getId(), session.getTokenHash(), reason, null, null));
    }

    @Override
    public void invalidateByTenant(Long tenantId, SessionInvalidReason reason) {
        List<LoginSessionEntity> sessions = loginSessionMapper.selectList(
                Wrappers.<LoginSessionEntity>lambdaQuery()
                        .eq(LoginSessionEntity::getTenantId, tenantId)
                        .eq(LoginSessionEntity::getStatus, SessionStatus.ACTIVE));
        sessions.forEach(session -> invalidate(session.getId(), session.getTokenHash(), reason, null, null));
    }

    @Override
    public PageResult<SessionVO> page(SessionQuery query) {
        Page<LoginSessionEntity> page = new Page<>(query.resolvePageNum(), query.resolvePageSize());
        Page<LoginSessionEntity> result = (Page<LoginSessionEntity>) loginSessionMapper
                .selectSessionPage(page, buildWrapper(query));
        Long currentSessionId = RequestContextHolder.get().getSessionId();
        List<SessionVO> rows = result.getRecords().stream()
                .map(entity -> toVO(entity, currentSessionId)).collect(Collectors.toList());
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), rows);
    }

    /** 强制下线：只处理管理范围内的有效会话，一次一条，当前会话不作为目标。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forceLogout(Long sessionId, ForceLogoutRequest request) {
        LoginSessionEntity session = loginSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new NotFoundException("msg.session.notFound");
        }
        RequestContext context = RequestContextHolder.get();
        if (sessionId.equals(context.getSessionId())) {
            throw new BizException(IamErrorCode.SESSION_003);
        }
        DataScopeAssert.within(session.getTenantId(), session.getOrgPath());
        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new BizException(IamErrorCode.SESSION_001);
        }
        invalidate(sessionId, session.getTokenHash(), SessionInvalidReason.FORCE_LOGOUT,
                context.getMemberId(), request.getReason());
    }

    @Override
    public String export(SessionQuery query) {
        List<LoginSessionEntity> sessions = loginSessionMapper
                .selectSessionPage(new Page<>(1, 10000), buildWrapper(query)).getRecords();
        List<List<Object>> rows = sessions.stream().map(session -> List.<Object>of(
                text(session.getAccount()),
                text(session.getNickname()),
                text(orgName(session.getOrgId())),
                text(I18nMessages.label(session.getEntry())),
                text(session.getClientIp()),
                text(session.getUserAgent()),
                UtcTimes.formatCompact(session.getLoginTime()),
                UtcTimes.formatCompact(session.getLastActiveTime()))).collect(Collectors.toList());
        return excelExportService.export("session", RequestContextHolder.get().getTenantId(),
                "export.sheet.session",
                List.of("export.column.account", "export.column.nickname", "export.column.orgName",
                        "export.column.entry", "export.column.clientIp", "export.column.userAgent",
                        "export.column.loginTime", "export.column.lastActiveTime"),
                rows);
    }

    @Override
    public int cleanExpired() {
        List<LoginSessionEntity> sessions = loginSessionMapper.selectList(
                Wrappers.<LoginSessionEntity>lambdaQuery()
                        .eq(LoginSessionEntity::getStatus, SessionStatus.ACTIVE)
                        .lt(LoginSessionEntity::getExpireTime, UtcTimes.now()));
        sessions.forEach(session -> invalidate(session.getId(), session.getTokenHash(),
                SessionInvalidReason.TIMEOUT, null, null));
        return sessions.size();
    }

    /** 会话失效按「状态 = 有效」条件更新，重复执行不覆盖已有失效原因；Redis 键删除为幂等操作。 */
    private void invalidate(Long sessionId, String tokenHash, SessionInvalidReason reason,
                            Long operatorId, String remark) {
        if (tokenHash != null) {
            redisService.delete(SESSION_KEY_PREFIX + tokenHash);
        }
        if (sessionId == null) {
            return;
        }
        LoginSessionEntity update = new LoginSessionEntity();
        update.setStatus(SessionStatus.INVALID);
        update.setInvalidReason(reason);
        update.setInvalidTime(UtcTimes.now());
        update.setInvalidBy(operatorId);
        update.setInvalidRemark(remark);
        loginSessionMapper.update(update, Wrappers.<LoginSessionEntity>lambdaUpdate()
                .eq(LoginSessionEntity::getId, sessionId)
                .eq(LoginSessionEntity::getStatus, SessionStatus.ACTIVE));
    }

    private LambdaQueryWrapper<LoginSessionEntity> buildWrapper(SessionQuery query) {
        LambdaQueryWrapper<LoginSessionEntity> wrapper = Wrappers.<LoginSessionEntity>lambdaQuery()
                .eq(LoginSessionEntity::getStatus, SessionStatus.ACTIVE);
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            String keyword = query.getKeyword().trim();
            wrapper.and(inner -> inner.like(LoginSessionEntity::getAccount, keyword)
                    .or().like(LoginSessionEntity::getNickname, keyword)
                    .or().like(LoginSessionEntity::getClientIp, keyword));
        }
        if (query.getEntry() != null) {
            wrapper.eq(LoginSessionEntity::getEntry, query.getEntry());
        }
        if (query.getOrgId() != null) {
            OrgEntity org = orgMapper.selectById(query.getOrgId());
            if (org != null) {
                wrapper.likeRight(LoginSessionEntity::getOrgPath, org.getOrgPath());
            }
        }
        wrapper.orderByDesc(LoginSessionEntity::getLastActiveTime);
        return wrapper;
    }

    private SessionVO toVO(LoginSessionEntity entity, Long currentSessionId) {
        SessionVO vo = new SessionVO();
        vo.setId(entity.getId());
        vo.setMemberId(entity.getMemberId());
        vo.setAccount(entity.getAccount());
        vo.setNickname(entity.getNickname());
        vo.setOrgId(entity.getOrgId());
        vo.setOrgName(orgName(entity.getOrgId()));
        vo.setEntry(entity.getEntry() == null ? null : entity.getEntry().getCode());
        vo.setEntryLabel(I18nMessages.label(entity.getEntry()));
        vo.setClientIp(entity.getClientIp());
        vo.setUserAgent(entity.getUserAgent());
        vo.setLoginTime(entity.getLoginTime());
        vo.setLastActiveTime(entity.getLastActiveTime());
        vo.setCurrent(entity.getId().equals(currentSessionId));
        return vo;
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }

    private String orgName(Long orgId) {
        OrgEntity org = orgMapper.selectById(orgId);
        return org == null ? null : org.getName();
    }

}

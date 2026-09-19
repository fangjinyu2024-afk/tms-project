package com.zxinfotek.tms.core.audit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zxinfotek.tms.common.model.PageResult;
import com.zxinfotek.tms.common.util.UtcTimes;
import com.zxinfotek.tms.core.audit.api.LoginLogService;
import com.zxinfotek.tms.core.audit.api.model.LoginLogQuery;
import com.zxinfotek.tms.core.audit.api.model.LoginLogRecord;
import com.zxinfotek.tms.core.audit.api.model.LoginLogVO;
import com.zxinfotek.tms.core.audit.entity.LoginLogEntity;
import com.zxinfotek.tms.core.audit.mapper.LoginLogMapper;
import com.zxinfotek.tms.infra.context.RequestContextHolder;
import com.zxinfotek.tms.infra.excel.ExcelExportService;
import com.zxinfotek.tms.infra.i18n.I18nMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * 登录日志：登录成功与失败均记录，只读保留（详细设计 3.7）。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
@Service
public class LoginLogServiceImpl implements LoginLogService {

    private static final Logger log = LoggerFactory.getLogger(LoginLogServiceImpl.class);

    private final LoginLogMapper loginLogMapper;
    private final ThreadPoolExecutor operLogExecutor;
    private final ExcelExportService excelExportService;

    public LoginLogServiceImpl(LoginLogMapper loginLogMapper,
                               @Qualifier("operLogExecutor") ThreadPoolExecutor operLogExecutor,
                               ExcelExportService excelExportService) {
        this.loginLogMapper = loginLogMapper;
        this.operLogExecutor = operLogExecutor;
        this.excelExportService = excelExportService;
    }

    @Override
    public void record(LoginLogRecord record) {
        String traceId = RequestContextHolder.get().getTraceId();
        LocalDateTime loginTime = UtcTimes.now();
        operLogExecutor.execute(() -> {
            try {
                LoginLogEntity entity = new LoginLogEntity();
                entity.setTenantId(record.getTenantId() == null ? 0L : record.getTenantId());
                entity.setOrgId(record.getOrgId());
                entity.setOrgPath(record.getOrgPath());
                entity.setMemberId(record.getMemberId());
                entity.setAccount(record.getAccount());
                entity.setEntry(record.getEntry());
                entity.setResult(record.getResult());
                entity.setFailReason(record.getFailReason());
                entity.setClientIp(record.getClientIp());
                entity.setUserAgent(record.getUserAgent());
                entity.setTraceId(traceId);
                entity.setLoginTime(loginTime);
                loginLogMapper.insert(entity);
            } catch (RuntimeException e) {
                log.error("登录日志写入失败，account={}", record.getAccount(), e);
            }
        });
    }

    @Override
    public PageResult<LoginLogVO> page(LoginLogQuery query) {
        Page<LoginLogEntity> page = new Page<>(query.resolvePageNum(), query.resolvePageSize());
        Page<LoginLogEntity> result = (Page<LoginLogEntity>) loginLogMapper
                .selectLoginLogPage(page, buildWrapper(query));
        List<LoginLogVO> rows = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), rows);
    }

    @Override
    public String export(LoginLogQuery query) {
        List<LoginLogEntity> logs = loginLogMapper
                .selectLoginLogPage(new Page<>(1, 10000), buildWrapper(query)).getRecords();
        List<List<Object>> rows = logs.stream().map(entity -> List.<Object>of(
                text(entity.getAccount()),
                text(I18nMessages.label(entity.getEntry())),
                text(I18nMessages.label(entity.getResult())),
                text(I18nMessages.getOrDefault(entity.getFailReason(), entity.getFailReason())),
                text(entity.getClientIp()),
                text(entity.getUserAgent()),
                UtcTimes.formatCompact(entity.getLoginTime()))).collect(Collectors.toList());
        return excelExportService.export("login-log", RequestContextHolder.get().getTenantId(),
                "export.sheet.loginLog",
                List.of("export.column.account", "export.column.entry", "export.column.result",
                        "export.column.failReason", "export.column.clientIp", "export.column.userAgent",
                        "export.column.loginTime"),
                rows);
    }

    private LambdaQueryWrapper<LoginLogEntity> buildWrapper(LoginLogQuery query) {
        LambdaQueryWrapper<LoginLogEntity> wrapper = Wrappers.lambdaQuery();
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            String keyword = query.getKeyword().trim();
            wrapper.and(inner -> inner.like(LoginLogEntity::getAccount, keyword)
                    .or().like(LoginLogEntity::getClientIp, keyword));
        }
        if (query.getResult() != null) {
            wrapper.eq(LoginLogEntity::getResult, query.getResult());
        }
        if (query.getEntry() != null) {
            wrapper.eq(LoginLogEntity::getEntry, query.getEntry());
        }
        if (query.getStartTime() != null) {
            wrapper.ge(LoginLogEntity::getLoginTime, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            wrapper.le(LoginLogEntity::getLoginTime, query.getEndTime());
        }
        wrapper.orderByDesc(LoginLogEntity::getLoginTime);
        return wrapper;
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }

    private LoginLogVO toVO(LoginLogEntity entity) {
        LoginLogVO vo = new LoginLogVO();
        vo.setId(entity.getId());
        vo.setAccount(entity.getAccount());
        vo.setMemberId(entity.getMemberId());
        vo.setOrgId(entity.getOrgId());
        vo.setEntry(entity.getEntry() == null ? null : entity.getEntry().getCode());
        vo.setEntryLabel(I18nMessages.label(entity.getEntry()));
        vo.setResult(entity.getResult() == null ? null : entity.getResult().getCode());
        vo.setResultLabel(I18nMessages.label(entity.getResult()));
        vo.setFailReason(I18nMessages.getOrDefault(entity.getFailReason(), entity.getFailReason()));
        vo.setClientIp(entity.getClientIp());
        vo.setUserAgent(entity.getUserAgent());
        vo.setLoginTime(entity.getLoginTime());
        return vo;
    }

}

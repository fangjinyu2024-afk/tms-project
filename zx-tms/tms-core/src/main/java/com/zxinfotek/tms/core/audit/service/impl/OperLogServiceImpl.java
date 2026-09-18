package com.zxinfotek.tms.core.audit.service.impl;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zxinfotek.tms.common.exception.NotFoundException;
import com.zxinfotek.tms.common.model.PageResult;
import com.zxinfotek.tms.common.util.UtcTimes;
import com.zxinfotek.tms.core.audit.api.OperLogService;
import com.zxinfotek.tms.core.audit.api.model.OperLogQuery;
import com.zxinfotek.tms.core.audit.api.model.OperLogRecord;
import com.zxinfotek.tms.core.audit.api.model.OperLogVO;
import com.zxinfotek.tms.core.audit.entity.OperLogEntity;
import com.zxinfotek.tms.core.audit.mapper.OperLogMapper;
import com.zxinfotek.tms.infra.context.DataScopeAssert;
import com.zxinfotek.tms.infra.context.RequestContext;
import com.zxinfotek.tms.infra.context.RequestContextHolder;
import com.zxinfotek.tms.infra.excel.ExcelExportService;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * 业务操作日志：以业务操作为记录单位异步落库，写入失败不影响主业务事务（详细设计 3.6）。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
@Service
public class OperLogServiceImpl implements OperLogService {

    private static final Logger log = LoggerFactory.getLogger(OperLogServiceImpl.class);

    private final OperLogMapper operLogMapper;
    private final ThreadPoolExecutor operLogExecutor;
    private final ExcelExportService excelExportService;

    public OperLogServiceImpl(OperLogMapper operLogMapper,
                              @Qualifier("operLogExecutor") ThreadPoolExecutor operLogExecutor,
                              ExcelExportService excelExportService) {
        this.operLogMapper = operLogMapper;
        this.operLogExecutor = operLogExecutor;
        this.excelExportService = excelExportService;
    }

    /**
     * 写入操作日志：在调用线程上取请求上下文快照后异步落库，按关联编号加模块加操作类型去重。
     *
     * @author zxinfotek
     * @since 2026-09-18
     */
    @Override
    public void record(OperLogRecord record) {
        RequestContext context = RequestContextHolder.get();
        OperLogEntity entity = new OperLogEntity();
        entity.setTenantId(context.getTenantId() == null ? 0L : context.getTenantId());
        entity.setOrgId(context.getOrgId());
        entity.setOrgPath(context.getOrgPath());
        entity.setMemberId(context.getMemberId());
        entity.setAccount(context.getAccount());
        entity.setNickname(context.getNickname());
        entity.setModule(record.getModule());
        entity.setAction(record.getAction());
        entity.setObjectType(record.getObjectType());
        entity.setObjectId(record.getObjectId());
        entity.setObjectName(record.getObjectName());
        entity.setResult(record.getResult());
        entity.setFailReason(record.getFailReason());
        entity.setTotalCount(record.getTotalCount());
        entity.setSuccessCount(record.getSuccessCount());
        entity.setChangeSummary(record.getChangeSummary());
        entity.setClientIp(context.getClientIp());
        entity.setTraceId(context.getTraceId());
        entity.setOperTime(UtcTimes.now());

        operLogExecutor.execute(() -> {
            try {
                operLogMapper.insert(entity);
            } catch (DuplicateKeyException e) {
                log.debug("同一关联编号的操作日志已存在，traceId={}", entity.getTraceId());
            } catch (RuntimeException e) {
                log.error("操作日志写入失败，traceId={}，模块={}", entity.getTraceId(), entity.getModule(), e);
            }
        });
    }

    @Override
    public PageResult<OperLogVO> page(OperLogQuery query) {
        Page<OperLogEntity> page = new Page<>(query.resolvePageNum(), query.resolvePageSize());
        Page<OperLogEntity> result = (Page<OperLogEntity>) operLogMapper
                .selectOperLogPage(page, buildWrapper(query));
        List<OperLogVO> rows = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), rows);
    }

    @Override
    public OperLogVO detail(Long id) {
        OperLogEntity entity = operLogMapper.selectById(id);
        if (entity == null) {
            throw new NotFoundException("操作日志不存在");
        }
        DataScopeAssert.within(entity.getTenantId(), entity.getOrgPath());
        return toVO(entity);
    }

    @Override
    public String export(OperLogQuery query) {
        List<OperLogEntity> logs = operLogMapper
                .selectOperLogPage(new Page<>(1, 10000), buildWrapper(query)).getRecords();
        List<OperLogExportRow> rows = logs.stream().map(entity -> {
            OperLogExportRow row = new OperLogExportRow();
            row.setModule(entity.getModule() == null ? "" : entity.getModule().getLabel());
            row.setAction(entity.getAction() == null ? "" : entity.getAction().getLabel());
            row.setObjectName(entity.getObjectName());
            row.setResult(entity.getResult() == null ? "" : entity.getResult().getLabel());
            row.setFailReason(entity.getFailReason());
            row.setAccount(entity.getAccount());
            row.setNickname(entity.getNickname());
            row.setClientIp(entity.getClientIp());
            row.setOperTime(entity.getOperTime());
            return row;
        }).collect(Collectors.toList());
        return excelExportService.export("oper-log", RequestContextHolder.get().getTenantId(),
                "操作日志", OperLogExportRow.class, rows);
    }

    private LambdaQueryWrapper<OperLogEntity> buildWrapper(OperLogQuery query) {
        LambdaQueryWrapper<OperLogEntity> wrapper = Wrappers.lambdaQuery();
        if (query.getModule() != null) {
            wrapper.eq(OperLogEntity::getModule, query.getModule());
        }
        if (query.getAction() != null) {
            wrapper.eq(OperLogEntity::getAction, query.getAction());
        }
        if (query.getResult() != null) {
            wrapper.eq(OperLogEntity::getResult, query.getResult());
        }
        if (query.getObjectType() != null && !query.getObjectType().isBlank()) {
            wrapper.eq(OperLogEntity::getObjectType, query.getObjectType());
        }
        if (query.getObjectId() != null && !query.getObjectId().isBlank()) {
            wrapper.eq(OperLogEntity::getObjectId, query.getObjectId());
        }
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            String keyword = query.getKeyword().trim();
            wrapper.and(inner -> inner.like(OperLogEntity::getAccount, keyword)
                    .or().like(OperLogEntity::getNickname, keyword)
                    .or().like(OperLogEntity::getObjectName, keyword));
        }
        if (query.getStartTime() != null) {
            wrapper.ge(OperLogEntity::getOperTime, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            wrapper.le(OperLogEntity::getOperTime, query.getEndTime());
        }
        wrapper.orderByDesc(OperLogEntity::getOperTime);
        return wrapper;
    }

    private OperLogVO toVO(OperLogEntity entity) {
        OperLogVO vo = new OperLogVO();
        vo.setId(entity.getId());
        vo.setModule(entity.getModule() == null ? null : entity.getModule().name());
        vo.setModuleLabel(entity.getModule() == null ? null : entity.getModule().getLabel());
        vo.setAction(entity.getAction() == null ? null : entity.getAction().name());
        vo.setActionLabel(entity.getAction() == null ? null : entity.getAction().getLabel());
        vo.setObjectType(entity.getObjectType());
        vo.setObjectId(entity.getObjectId());
        vo.setObjectName(entity.getObjectName());
        vo.setResult(entity.getResult() == null ? null : entity.getResult().name());
        vo.setResultLabel(entity.getResult() == null ? null : entity.getResult().getLabel());
        vo.setFailReason(entity.getFailReason());
        vo.setTotalCount(entity.getTotalCount());
        vo.setSuccessCount(entity.getSuccessCount());
        vo.setChangeSummary(entity.getChangeSummary());
        vo.setMemberId(entity.getMemberId());
        vo.setAccount(entity.getAccount());
        vo.setNickname(entity.getNickname());
        vo.setOrgId(entity.getOrgId());
        vo.setClientIp(entity.getClientIp());
        vo.setTraceId(entity.getTraceId());
        vo.setOperTime(entity.getOperTime());
        return vo;
    }

    @Data
    public static class OperLogExportRow {
        @ExcelProperty("业务模块")
        private String module;
        @ExcelProperty("操作类型")
        private String action;
        @ExcelProperty("业务对象")
        private String objectName;
        @ExcelProperty("结果")
        private String result;
        @ExcelProperty("失败原因")
        private String failReason;
        @ExcelProperty("账号")
        private String account;
        @ExcelProperty("昵称")
        private String nickname;
        @ExcelProperty("来源 IP")
        private String clientIp;
        @ExcelProperty("操作时间")
        private LocalDateTime operTime;
    }
}

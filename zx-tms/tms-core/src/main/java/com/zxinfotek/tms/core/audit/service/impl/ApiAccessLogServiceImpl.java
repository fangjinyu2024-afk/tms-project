package com.zxinfotek.tms.core.audit.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zxinfotek.tms.common.util.UtcTimes;
import com.zxinfotek.tms.core.audit.api.ApiAccessLogService;
import com.zxinfotek.tms.core.audit.api.model.ApiAccessLogRecord;
import com.zxinfotek.tms.core.audit.entity.ApiAccessLogEntity;
import com.zxinfotek.tms.core.audit.mapper.ApiAccessLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class ApiAccessLogServiceImpl implements ApiAccessLogService {

    private static final Logger log = LoggerFactory.getLogger(ApiAccessLogServiceImpl.class);

    private final ApiAccessLogMapper apiAccessLogMapper;
    private final ThreadPoolExecutor apiAccessLogExecutor;

    public ApiAccessLogServiceImpl(ApiAccessLogMapper apiAccessLogMapper,
                                   @Qualifier("apiAccessLogExecutor") ThreadPoolExecutor apiAccessLogExecutor) {
        this.apiAccessLogMapper = apiAccessLogMapper;
        this.apiAccessLogExecutor = apiAccessLogExecutor;
    }

    @Override
    public void record(ApiAccessLogRecord record) {
        LocalDateTime createTime = UtcTimes.now();
        apiAccessLogExecutor.execute(() -> {
            try {
                ApiAccessLogEntity entity = new ApiAccessLogEntity();
                entity.setTraceId(record.getTraceId());
                entity.setMemberId(record.getMemberId());
                entity.setMethod(record.getMethod());
                entity.setPath(record.getPath());
                entity.setHttpStatus(record.getHttpStatus());
                entity.setBizCode(record.getBizCode());
                entity.setDurationMs(record.getDurationMs());
                entity.setClientIp(record.getClientIp());
                entity.setCreateTime(createTime);
                apiAccessLogMapper.insert(entity);
            } catch (RuntimeException e) {
                log.error("接口访问日志写入失败，traceId={}", record.getTraceId(), e);
            }
        });
    }

    @Override
    public int cleanBefore(int retentionDays) {
        LocalDateTime deadline = UtcTimes.now().minusDays(retentionDays);
        return apiAccessLogMapper.delete(Wrappers.<ApiAccessLogEntity>lambdaQuery()
                .lt(ApiAccessLogEntity::getCreateTime, deadline));
    }
}

package com.zxinfotek.tms.admin.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zxinfotek.tms.common.enums.OperResult;
import com.zxinfotek.tms.common.exception.BaseException;
import com.zxinfotek.tms.core.audit.api.OperLogContext;
import com.zxinfotek.tms.core.audit.api.OperLogService;
import com.zxinfotek.tms.core.audit.api.model.OperLogRecord;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 业务操作日志切面：以业务方法为单位记录操作主体、对象、结果与字段白名单内的变更摘要。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
@Aspect
@Component
@Order(20)
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    private final OperLogService operLogService;
    private final ObjectMapper objectMapper;

    public OperationLogAspect(OperLogService operLogService, ObjectMapper objectMapper) {
        this.operLogService = operLogService;
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        OperLogContext.drain();
        Object result = null;
        String failReason = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (BaseException e) {
            failReason = e.getMessage();
            throw e;
        } catch (Throwable e) {
            failReason = "系统内部错误";
            throw e;
        } finally {
            try {
                write(operationLog, joinPoint, result, failReason);
            } catch (RuntimeException e) {
                log.error("操作日志记录失败，模块={}", operationLog.module(), e);
            }
        }
    }

    private void write(OperationLog annotation, ProceedingJoinPoint joinPoint,
                       Object returned, String failReason) {
        OperLogContext.Holder holder = OperLogContext.drain();
        OperLogRecord record = new OperLogRecord();
        record.setModule(annotation.module());
        record.setAction(annotation.action());
        record.setObjectType(annotation.objectType().isEmpty() ? null : annotation.objectType());
        record.setObjectId(objectId(joinPoint, returned));
        record.setFailReason(failReason);
        record.setResult(failReason == null ? OperResult.SUCCESS : OperResult.FAIL);
        record.setChangeSummary(changeSummary(annotation, joinPoint));

        if (holder != null) {
            if (holder.getObjectType() != null) {
                record.setObjectType(holder.getObjectType());
            }
            if (holder.getObjectId() != null) {
                record.setObjectId(holder.getObjectId());
            }
            record.setObjectName(holder.getObjectName());
            if (holder.getChangeSummary() != null) {
                record.setChangeSummary(holder.getChangeSummary());
            }
            record.setTotalCount(holder.getTotalCount());
            record.setSuccessCount(holder.getSuccessCount());
            if (failReason == null && holder.getResult() != null) {
                record.setResult(holder.getResult());
            }
        }
        operLogService.record(record);
    }

    /** 业务对象编号优先取路径参数中的主键，其次取返回值中的主键。 */
    private String objectId(ProceedingJoinPoint joinPoint, Object returned) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof Long id) {
                return String.valueOf(id);
            }
        }
        if (returned instanceof com.zxinfotek.tms.common.model.Result<?> result
                && result.getData() instanceof Long id) {
            return String.valueOf(id);
        }
        return null;
    }

    /** 变更摘要只记录注解声明的字段白名单，其余字段不落库。 */
    private String changeSummary(OperationLog annotation, ProceedingJoinPoint joinPoint) {
        if (annotation.summaryFields().length == 0) {
            return null;
        }
        Map<String, Object> summary = new LinkedHashMap<>();
        for (Object arg : joinPoint.getArgs()) {
            if (arg == null || arg instanceof Long || arg instanceof String) {
                continue;
            }
            for (String field : annotation.summaryFields()) {
                Object value = readProperty(arg, field);
                if (value != null) {
                    summary.put(field, value);
                }
            }
        }
        if (summary.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(summary);
        } catch (Exception e) {
            log.warn("变更摘要序列化失败", e);
            return null;
        }
    }

    private Object readProperty(Object target, String field) {
        try {
            PropertyDescriptor descriptor = Arrays
                    .stream(java.beans.Introspector.getBeanInfo(target.getClass()).getPropertyDescriptors())
                    .filter(item -> item.getName().equals(field))
                    .findFirst().orElse(null);
            if (descriptor == null || descriptor.getReadMethod() == null) {
                return null;
            }
            return descriptor.getReadMethod().invoke(target);
        } catch (Exception e) {
            return null;
        }
    }
}

package com.zxinfotek.tms.admin.filter;

import com.zxinfotek.tms.common.util.IdUtils;
import com.zxinfotek.tms.core.audit.api.ApiAccessLogService;
import com.zxinfotek.tms.core.audit.api.model.ApiAccessLogRecord;
import com.zxinfotek.tms.infra.context.RequestContext;
import com.zxinfotek.tms.infra.context.RequestContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 请求入口：生成关联编号、建立请求上下文，并在响应完成后写入接口访问日志（详细设计 7.5）。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
@Component
@Order(1)
public class TraceFilter extends OncePerRequestFilter {

    private static final String TRACE_HEADER = "X-Trace-Id";

    private final ApiAccessLogService apiAccessLogService;

    public TraceFilter(ApiAccessLogService apiAccessLogService) {
        this.apiAccessLogService = apiAccessLogService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        String traceId = IdUtils.traceId();
        RequestContext context = new RequestContext();
        context.setTraceId(traceId);
        context.setClientIp(clientIp(request));
        context.setUserAgent(truncate(request.getHeader("User-Agent")));
        RequestContextHolder.set(context);
        MDC.put("traceId", traceId);
        response.setHeader(TRACE_HEADER, traceId);
        try {
            chain.doFilter(request, response);
        } finally {
            writeAccessLog(request, response, context, (int) (System.currentTimeMillis() - start));
            RequestContextHolder.clear();
            MDC.remove("traceId");
        }
    }

    private void writeAccessLog(HttpServletRequest request, HttpServletResponse response,
                                RequestContext context, int duration) {
        if (!request.getRequestURI().startsWith("/api/")) {
            return;
        }
        ApiAccessLogRecord record = new ApiAccessLogRecord();
        record.setTraceId(context.getTraceId());
        record.setMemberId(context.getMemberId());
        record.setMethod(request.getMethod());
        record.setPath(truncate(request.getRequestURI()));
        record.setHttpStatus(response.getStatus());
        record.setBizCode(response.getHeader("X-Biz-Code"));
        record.setDurationMs(duration);
        record.setClientIp(context.getClientIp());
        apiAccessLogService.record(record);
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int comma = forwarded.indexOf(',');
            return comma > 0 ? forwarded.substring(0, comma).trim() : forwarded.trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        return realIp != null && !realIp.isBlank() ? realIp.trim() : request.getRemoteAddr();
    }

    private static String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() > 255 ? value.substring(0, 255) : value;
    }
}

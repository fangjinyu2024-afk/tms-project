package com.zxinfotek.tms.admin.schedule;

import com.zxinfotek.tms.core.audit.api.ApiAccessLogService;
import com.zxinfotek.tms.core.iam.api.LoginSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 后台定时任务：过期会话清理与接口访问日志清理，时间口径按 UTC。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
@Component
public class MaintenanceTasks {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceTasks.class);

    private final LoginSessionService loginSessionService;
    private final ApiAccessLogService apiAccessLogService;
    private final int apiLogRetentionDays;

    public MaintenanceTasks(LoginSessionService loginSessionService,
                            ApiAccessLogService apiAccessLogService,
                            @Value("${tms.api-log.retention-days:30}") int apiLogRetentionDays) {
        this.loginSessionService = loginSessionService;
        this.apiAccessLogService = apiAccessLogService;
        this.apiLogRetentionDays = apiLogRetentionDays;
    }

    @Scheduled(cron = "${tms.schedule.session-clean-cron:0 */5 * * * ?}", zone = "UTC")
    public void cleanExpiredSessions() {
        int count = loginSessionService.cleanExpired();
        if (count > 0) {
            log.info("清理过期会话 {} 条", count);
        }
    }

    @Scheduled(cron = "${tms.schedule.api-log-clean-cron:0 30 1 * * ?}", zone = "UTC")
    public void cleanApiAccessLogs() {
        int count = apiAccessLogService.cleanBefore(apiLogRetentionDays);
        if (count > 0) {
            log.info("清理接口访问日志 {} 条", count);
        }
    }
}

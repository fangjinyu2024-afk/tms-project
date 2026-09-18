package com.zxinfotek.tms.core.audit.api;

import com.zxinfotek.tms.core.audit.api.model.ApiAccessLogRecord;

public interface ApiAccessLogService {

    /** 异步写入接口访问日志，队列满时丢弃并输出告警。 */
    void record(ApiAccessLogRecord record);

    /** 清理保留期之外的接口访问日志，由定时任务调用。 */
    int cleanBefore(int retentionDays);
}

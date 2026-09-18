package com.zxinfotek.tms.core.audit.api.model;

import lombok.Data;

@Data
public class ApiAccessLogRecord {

    private String traceId;
    private Long memberId;
    private String method;
    private String path;
    private Integer httpStatus;
    private String bizCode;
    private Integer durationMs;
    private String clientIp;
}

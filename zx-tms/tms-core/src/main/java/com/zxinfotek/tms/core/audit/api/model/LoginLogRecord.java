package com.zxinfotek.tms.core.audit.api.model;

import com.zxinfotek.tms.common.enums.LoginResult;
import com.zxinfotek.tms.common.enums.SessionEntry;
import lombok.Data;

@Data
public class LoginLogRecord {

    private Long tenantId;
    private Long orgId;
    private String orgPath;
    private Long memberId;
    private String account;
    private SessionEntry entry;
    private LoginResult result;
    private String failReason;
    private String clientIp;
    private String userAgent;
}

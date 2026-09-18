package com.zxinfotek.tms.core.audit.api.model;

import com.zxinfotek.tms.common.enums.LoginResult;
import com.zxinfotek.tms.common.enums.SessionEntry;
import com.zxinfotek.tms.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class LoginLogQuery extends PageQuery {

    private String keyword;
    private LoginResult result;
    private SessionEntry entry;
    private Long orgId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}

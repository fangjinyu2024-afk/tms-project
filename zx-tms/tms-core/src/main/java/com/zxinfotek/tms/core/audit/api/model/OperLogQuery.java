package com.zxinfotek.tms.core.audit.api.model;

import com.zxinfotek.tms.common.enums.LogModule;
import com.zxinfotek.tms.common.enums.OperAction;
import com.zxinfotek.tms.common.enums.OperResult;
import com.zxinfotek.tms.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class OperLogQuery extends PageQuery {

    private LogModule module;
    private OperAction action;
    private OperResult result;
    private String keyword;
    private String objectType;
    private String objectId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}

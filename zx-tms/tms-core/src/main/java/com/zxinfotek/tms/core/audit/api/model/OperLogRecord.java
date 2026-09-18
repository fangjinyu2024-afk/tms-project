package com.zxinfotek.tms.core.audit.api.model;

import com.zxinfotek.tms.common.enums.LogModule;
import com.zxinfotek.tms.common.enums.OperAction;
import com.zxinfotek.tms.common.enums.OperResult;
import lombok.Data;

@Data
public class OperLogRecord {

    private LogModule module;
    private OperAction action;
    private String objectType;
    private String objectId;
    private String objectName;
    private OperResult result;
    private String failReason;
    private Integer totalCount;
    private Integer successCount;
    /** 字段白名单内的变更前后摘要，敏感字段已脱敏 */
    private String changeSummary;
}

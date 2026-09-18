package com.zxinfotek.tms.core.audit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zxinfotek.tms.common.enums.LogModule;
import com.zxinfotek.tms.common.enums.OperAction;
import com.zxinfotek.tms.common.enums.OperResult;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_oper_log")
public class OperLogEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long tenantId;
    private Long orgId;
    private String orgPath;
    private Long memberId;
    private String account;
    private String nickname;
    private LogModule module;
    private OperAction action;
    private String objectType;
    private String objectId;
    private String objectName;
    private OperResult result;
    private String failReason;
    private Integer totalCount;
    private Integer successCount;
    private String changeSummary;
    private String clientIp;
    private String traceId;
    private LocalDateTime operTime;
}

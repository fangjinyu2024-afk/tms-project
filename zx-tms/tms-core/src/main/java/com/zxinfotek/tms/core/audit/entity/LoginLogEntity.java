package com.zxinfotek.tms.core.audit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zxinfotek.tms.common.enums.LoginResult;
import com.zxinfotek.tms.common.enums.SessionEntry;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_login_log")
public class LoginLogEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

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
    private String traceId;
    private LocalDateTime loginTime;
}

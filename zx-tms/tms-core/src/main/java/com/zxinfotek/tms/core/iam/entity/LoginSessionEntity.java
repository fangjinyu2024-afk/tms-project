package com.zxinfotek.tms.core.iam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zxinfotek.tms.common.enums.SessionEntry;
import com.zxinfotek.tms.common.enums.SessionInvalidReason;
import com.zxinfotek.tms.common.enums.SessionStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_login_session")
public class LoginSessionEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long tenantId;
    private Long orgId;
    private String orgPath;
    private Long memberId;
    private String account;
    private String nickname;
    private String tokenHash;
    private SessionEntry entry;
    private String clientIp;
    private String userAgent;
    private LocalDateTime loginTime;
    private LocalDateTime lastActiveTime;
    private LocalDateTime expireTime;
    private SessionStatus status;
    private SessionInvalidReason invalidReason;
    private LocalDateTime invalidTime;
    private Long invalidBy;
    private String invalidRemark;
}

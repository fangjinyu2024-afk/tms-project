package com.zxinfotek.tms.core.iam.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zxinfotek.tms.common.entity.BaseEntity;
import com.zxinfotek.tms.common.enums.EnableStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_member")
public class MemberEntity extends BaseEntity {

    private Long tenantId;
    private Long orgId;
    private String orgPath;
    private String account;
    private String accountLower;
    private String nickname;
    private String passwordHash;
    private String email;
    private Integer emailVerified;
    private String phone;
    private Integer mustChangePassword;
    private LocalDateTime passwordUpdatedAt;
    private Integer failCount;
    private LocalDateTime lockedUntil;
    private Integer permVersion;
    private EnableStatus status;

    @TableLogic
    private Integer deleted;
}

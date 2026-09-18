package com.zxinfotek.tms.core.iam.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.zxinfotek.tms.common.entity.BaseEntity;
import com.zxinfotek.tms.common.enums.DataScope;
import com.zxinfotek.tms.common.enums.EnableStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_role")
public class RoleEntity extends BaseEntity {

    private Long tenantId;
    private Long ownerOrgId;
    private String ownerOrgPath;
    private String name;
    private String description;
    private DataScope dataScope;
    private String builtinCode;
    private Integer permVersion;
    private EnableStatus status;

    @Version
    private Integer version;

    @TableLogic
    private Integer deleted;
}

package com.zxinfotek.tms.core.iam.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zxinfotek.tms.common.entity.BaseEntity;
import com.zxinfotek.tms.common.enums.EnableStatus;
import com.zxinfotek.tms.common.enums.OrgType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_org")
public class OrgEntity extends BaseEntity {

    private Long tenantId;
    private Long parentId;
    private String orgPath;
    private OrgType orgType;
    private String name;
    private String contactName;
    private String contactPhone;
    private EnableStatus status;

    @TableLogic
    private Integer deleted;
}

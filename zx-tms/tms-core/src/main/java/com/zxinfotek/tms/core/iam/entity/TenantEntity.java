package com.zxinfotek.tms.core.iam.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zxinfotek.tms.common.entity.BaseEntity;
import com.zxinfotek.tms.common.enums.EnableStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_tenant")
public class TenantEntity extends BaseEntity {

    private String name;
    private Long rootOrgId;
    private String contactName;
    private String contactPhone;
    private String country;
    private String province;
    private String city;
    private String remark;
    private EnableStatus status;
    private String authCodeChannel;
    private Integer featureVersion;

    @TableLogic
    private Integer deleted;
}

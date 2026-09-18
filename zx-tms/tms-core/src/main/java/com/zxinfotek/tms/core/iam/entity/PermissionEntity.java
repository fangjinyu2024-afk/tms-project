package com.zxinfotek.tms.core.iam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zxinfotek.tms.common.enums.EnableStatus;
import lombok.Data;

@Data
@TableName("t_permission")
public class PermissionEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String permCode;
    private String menuKey;
    private String menuName;
    private String groupName;
    private String action;
    private String actionName;
    private Integer platformOnly;
    private Integer sort;
    private EnableStatus status;
}

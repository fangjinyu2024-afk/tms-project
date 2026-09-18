package com.zxinfotek.tms.core.iam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_role_perm")
public class RolePermEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long roleId;
    private String permCode;
}

package com.zxinfotek.tms.core.iam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_member_role")
public class MemberRoleEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long memberId;
    private Long roleId;
}

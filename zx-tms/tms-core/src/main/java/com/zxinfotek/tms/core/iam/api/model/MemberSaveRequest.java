package com.zxinfotek.tms.core.iam.api.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class MemberSaveRequest {

    /** 所属机构，新增时必填；创建后不通过普通编辑跨机构移动人员 */
    private Long orgId;

    /** 登录账号，创建后不可更改 */
    private String account;

    @NotBlank(message = "请输入昵称")
    @Size(max = 50, message = "昵称最多 50 个字符")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    @Size(max = 128)
    private String email;

    @Size(max = 30)
    private String phone;

    @NotNull(message = "至少选择一个角色")
    private List<Long> roleIds;
}

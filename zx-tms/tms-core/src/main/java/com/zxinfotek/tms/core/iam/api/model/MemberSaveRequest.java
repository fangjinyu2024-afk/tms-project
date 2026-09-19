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

    @NotBlank(message = "msg.valid.nicknameRequired")
    @Size(max = 50, message = "msg.valid.nicknameSize")
    private String nickname;

    @Email(message = "msg.valid.emailInvalid")
    @Size(max = 128, message = "msg.valid.emailSize")
    private String email;

    @Size(max = 30, message = "msg.valid.phoneSize")
    private String phone;

    @NotNull(message = "msg.valid.rolesRequired")
    private List<Long> roleIds;
}

package com.zxinfotek.tms.core.iam.api.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "请输入昵称")
    @Size(max = 50, message = "昵称最多 50 个字符")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    @Size(max = 128)
    private String email;

    @Size(max = 30)
    private String phone;
}

package com.zxinfotek.tms.core.iam.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank(message = "重置令牌不能为空")
    private String token;

    @NotBlank(message = "请输入新密码")
    @Size(min = 8, max = 64, message = "新密码长度为 8 至 64 位")
    private String newPassword;
}

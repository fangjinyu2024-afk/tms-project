package com.zxinfotek.tms.core.iam.api.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequest {

    @NotBlank(message = "请输入登录账号")
    private String account;
}

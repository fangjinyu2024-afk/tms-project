package com.zxinfotek.tms.core.iam.api.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequest {

    @NotBlank(message = "msg.valid.accountRequired")
    private String account;

    @NotBlank(message = "msg.valid.captchaRequired")
    private String captchaId;

    @NotBlank(message = "msg.valid.captchaRequired")
    private String captchaCode;
}

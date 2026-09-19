package com.zxinfotek.tms.core.iam.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank(message = "msg.valid.resetTokenRequired")
    private String token;

    @NotBlank(message = "msg.valid.newPasswordRequired")
    @Size(min = 8, max = 64, message = "msg.valid.newPasswordLength")
    private String newPassword;
}

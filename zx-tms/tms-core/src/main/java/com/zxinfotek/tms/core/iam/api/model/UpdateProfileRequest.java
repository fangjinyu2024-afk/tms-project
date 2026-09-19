package com.zxinfotek.tms.core.iam.api.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "msg.valid.nicknameRequired")
    @Size(max = 50, message = "msg.valid.nicknameSize")
    private String nickname;

    @Email(message = "msg.valid.emailInvalid")
    @Size(max = 128, message = "msg.valid.emailSize")
    private String email;

    @Size(max = 30, message = "msg.valid.phoneSize")
    private String phone;
}

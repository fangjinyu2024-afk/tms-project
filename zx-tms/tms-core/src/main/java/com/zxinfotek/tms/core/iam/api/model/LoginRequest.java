package com.zxinfotek.tms.core.iam.api.model;

import com.zxinfotek.tms.common.enums.SessionEntry;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "msg.valid.accountRequired")
    private String account;

    @NotBlank(message = "msg.valid.passwordRequired")
    private String password;

    @NotNull(message = "msg.valid.entryRequired")
    private SessionEntry entry;
}

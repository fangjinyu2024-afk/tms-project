package com.zxinfotek.tms.core.iam.api.model;

import com.zxinfotek.tms.common.enums.SessionEntry;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "请输入登录账号")
    private String account;

    @NotBlank(message = "请输入密码")
    private String password;

    @NotNull(message = "请指定会话入口")
    private SessionEntry entry;
}

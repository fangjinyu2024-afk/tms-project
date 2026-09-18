package com.zxinfotek.tms.core.iam.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ForceLogoutRequest {

    @NotBlank(message = "请填写下线原因")
    @Size(max = 200)
    private String reason;
}

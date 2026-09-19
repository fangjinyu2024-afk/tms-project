package com.zxinfotek.tms.core.iam.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ForceLogoutRequest {

    @NotBlank(message = "msg.valid.forceReasonRequired")
    @Size(max = 200, message = "msg.valid.reasonSize")
    private String reason;
}

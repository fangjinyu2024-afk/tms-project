package com.zxinfotek.tms.core.iam.api.model;

import com.zxinfotek.tms.common.enums.EnableStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusRequest {

    @NotNull(message = "msg.valid.statusRequired")
    private EnableStatus status;
}

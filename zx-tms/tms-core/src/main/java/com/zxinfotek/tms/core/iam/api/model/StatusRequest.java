package com.zxinfotek.tms.core.iam.api.model;

import com.zxinfotek.tms.common.enums.EnableStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusRequest {

    @NotNull(message = "请指定目标状态")
    private EnableStatus status;
}

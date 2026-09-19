package com.zxinfotek.tms.core.iam.api.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class TenantFeatureSaveRequest {

    @NotNull(message = "msg.valid.menuKeysRequired")
    private List<String> menuKeys;
}

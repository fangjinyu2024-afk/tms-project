package com.zxinfotek.tms.core.iam.api.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class TenantFeatureSaveRequest {

    @NotNull(message = "请选择开通的菜单")
    private List<String> menuKeys;
}

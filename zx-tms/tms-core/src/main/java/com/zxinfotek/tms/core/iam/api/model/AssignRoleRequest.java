package com.zxinfotek.tms.core.iam.api.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AssignRoleRequest {

    @NotEmpty(message = "至少选择一个角色")
    private List<Long> roleIds;
}

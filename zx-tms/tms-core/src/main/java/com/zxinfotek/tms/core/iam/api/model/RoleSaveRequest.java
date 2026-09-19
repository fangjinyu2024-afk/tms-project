package com.zxinfotek.tms.core.iam.api.model;

import com.zxinfotek.tms.common.enums.DataScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RoleSaveRequest {

    @NotBlank(message = "msg.valid.roleNameRequired")
    @Size(max = 50, message = "msg.valid.roleNameSize")
    private String name;

    @Size(max = 200, message = "msg.valid.descriptionSize")
    private String description;

    /** 归属机构，新增时必填；编辑时忽略 */
    private Long ownerOrgId;

    @NotNull(message = "msg.valid.dataScopeRequired")
    private DataScope dataScope;

    private List<String> permCodes;

    /** 乐观锁版本，编辑时必填 */
    private Integer version;
}

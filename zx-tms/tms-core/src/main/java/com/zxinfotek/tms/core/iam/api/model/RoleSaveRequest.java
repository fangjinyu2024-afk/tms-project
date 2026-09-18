package com.zxinfotek.tms.core.iam.api.model;

import com.zxinfotek.tms.common.enums.DataScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RoleSaveRequest {

    @NotBlank(message = "请输入角色名称")
    @Size(max = 50, message = "角色名称最多 50 个字符")
    private String name;

    @Size(max = 200)
    private String description;

    /** 归属机构，新增时必填；编辑时忽略 */
    private Long ownerOrgId;

    @NotNull(message = "请选择可管理范围")
    private DataScope dataScope;

    private List<String> permCodes;

    /** 乐观锁版本，编辑时必填 */
    private Integer version;
}

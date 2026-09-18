package com.zxinfotek.tms.core.iam.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class OrgSaveRequest {

    /** 上级机构，新增时必填；本期不通过普通编辑变更既有机构的上级归属 */
    private Long parentId;

    @NotBlank(message = "请输入机构名称")
    @Size(max = 100, message = "机构名称最多 100 个字符")
    private String name;

    @Size(max = 50)
    private String contactName;

    @Size(max = 30)
    private String contactPhone;

    /** 以下仅新增机构时使用：同事务创建的机构管理员 */
    private String adminAccount;
    private String adminNickname;
    private List<Long> adminRoleIds;
}

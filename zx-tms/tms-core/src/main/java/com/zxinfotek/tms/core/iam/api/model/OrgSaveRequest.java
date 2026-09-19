package com.zxinfotek.tms.core.iam.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class OrgSaveRequest {

    /** 上级机构，新增时必填；本期不通过普通编辑变更既有机构的上级归属 */
    private Long parentId;

    @NotBlank(message = "msg.valid.orgNameRequired")
    @Size(max = 100, message = "msg.valid.orgNameSize")
    private String name;

    @Size(max = 50, message = "msg.valid.contactNameSize")
    private String contactName;

    @Size(max = 30, message = "msg.valid.contactPhoneSize")
    private String contactPhone;

    /** 以下仅新增机构时使用：同事务创建的机构管理员 */
    private String adminAccount;
    private String adminNickname;
    private List<Long> adminRoleIds;
}

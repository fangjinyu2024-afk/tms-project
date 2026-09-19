package com.zxinfotek.tms.core.iam.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class TenantSaveRequest {

    @NotBlank(message = "msg.valid.tenantNameRequired")
    @Size(max = 100, message = "msg.valid.tenantNameSize")
    private String name;

    @Size(max = 50, message = "msg.valid.contactNameSize")
    private String contactName;

    @Size(max = 30, message = "msg.valid.contactPhoneSize")
    private String contactPhone;

    @Size(max = 50, message = "msg.valid.regionSize")
    private String country;

    @Size(max = 50, message = "msg.valid.regionSize")
    private String province;

    @Size(max = 50, message = "msg.valid.regionSize")
    private String city;

    @Size(max = 500, message = "msg.valid.remarkSize")
    private String remark;

    /** 授权码发放渠道，取值见详细设计 6.3.9 */
    private String authCodeChannel;

    private List<Long> modelIds;

    /** 以下仅新增客户时使用：客户管理员账号与本次开通的菜单 */
    private String adminAccount;
    private String adminNickname;
    private List<String> menuKeys;
}

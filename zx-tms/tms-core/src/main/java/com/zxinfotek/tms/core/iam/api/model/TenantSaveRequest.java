package com.zxinfotek.tms.core.iam.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class TenantSaveRequest {

    @NotBlank(message = "请输入客户名称")
    @Size(max = 100, message = "客户名称最多 100 个字符")
    private String name;

    @Size(max = 50)
    private String contactName;

    @Size(max = 30)
    private String contactPhone;

    @Size(max = 50)
    private String country;

    @Size(max = 50)
    private String province;

    @Size(max = 50)
    private String city;

    @Size(max = 500)
    private String remark;

    /** 授权码发放渠道，取值见详细设计 6.3.9 */
    private String authCodeChannel;

    private List<Long> modelIds;

    /** 以下仅新增客户时使用：客户管理员账号与本次开通的菜单 */
    private String adminAccount;
    private String adminNickname;
    private List<String> menuKeys;
}

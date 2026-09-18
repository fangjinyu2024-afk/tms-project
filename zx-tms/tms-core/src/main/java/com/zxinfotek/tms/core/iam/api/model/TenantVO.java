package com.zxinfotek.tms.core.iam.api.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TenantVO {

    private Long id;
    private String name;
    private Long rootOrgId;
    private String contactName;
    private String contactPhone;
    private String country;
    private String province;
    private String city;
    private String remark;
    private String status;
    private String authCodeChannel;
    private Integer featureVersion;
    private List<Long> modelIds;
    private List<String> modelNames;
    private List<String> menuKeys;
    private LocalDateTime createTime;
}

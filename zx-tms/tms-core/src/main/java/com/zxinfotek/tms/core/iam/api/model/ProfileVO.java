package com.zxinfotek.tms.core.iam.api.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ProfileVO {

    private MemberProfileVO member;
    private List<String> menus;
    private Map<String, String> permissions;
    private Boolean mustChangePassword;
}

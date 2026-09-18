package com.zxinfotek.tms.core.iam.api.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class LoginVO {

    private String token;
    private Integer expiresIn;
    private Boolean mustChangePassword;
    private MemberProfileVO member;
    private List<String> menus;
    private Map<String, String> permissions;
}

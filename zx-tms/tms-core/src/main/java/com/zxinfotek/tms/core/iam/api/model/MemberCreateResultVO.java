package com.zxinfotek.tms.core.iam.api.model;

import lombok.Data;

@Data
public class MemberCreateResultVO {

    private Long memberId;
    private String account;
    /** 系统生成的初始密码，只在本次响应返回一次，不落库明文、不写日志 */
    private String initialPassword;
}

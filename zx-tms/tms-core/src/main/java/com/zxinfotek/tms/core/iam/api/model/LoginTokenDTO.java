package com.zxinfotek.tms.core.iam.api.model;

import lombok.Data;

@Data
public class LoginTokenDTO {

    private Long sessionId;
    /** 明文令牌只在本次响应返回，服务端只保存 SHA-256 摘要 */
    private String token;
    private Integer expiresIn;
}

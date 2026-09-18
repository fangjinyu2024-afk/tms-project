package com.zxinfotek.tms.core.iam.api.model;

import lombok.Data;

@Data
public class EmailVerifyRequest {

    /** send 发起验证邮件，confirm 凭令牌确认 */
    private String action;
    private String token;
}

package com.zxinfotek.tms.core.iam.api.model;

import lombok.Data;

@Data
public class CaptchaVO {

    /** 验证码标识，登录与找回密码时原样回传 */
    private String captchaId;

    /** base64 图片，形如 data:image/png;base64,... */
    private String image;
}

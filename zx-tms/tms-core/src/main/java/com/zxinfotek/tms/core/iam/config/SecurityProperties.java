package com.zxinfotek.tms.core.iam.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "tms.security")
public class SecurityProperties {

    /** 连续登录失败锁定阈值 */
    private int loginFailThreshold = 5;

    /** 账号锁定时长 */
    private Duration lockDuration = Duration.ofMinutes(15);

    /** 找回密码令牌有效期 */
    private Duration resetTokenExpire = Duration.ofMinutes(30);

    /** 邮箱验证令牌有效期 */
    private Duration emailTokenExpire = Duration.ofHours(24);

    /** 找回密码与邮箱验证链接前缀 */
    private String consoleBaseUrl = "https://tms.example.com";

    public int getLoginFailThreshold() {
        return loginFailThreshold;
    }

    public void setLoginFailThreshold(int loginFailThreshold) {
        this.loginFailThreshold = loginFailThreshold;
    }

    public Duration getLockDuration() {
        return lockDuration;
    }

    public void setLockDuration(Duration lockDuration) {
        this.lockDuration = lockDuration;
    }

    public Duration getResetTokenExpire() {
        return resetTokenExpire;
    }

    public void setResetTokenExpire(Duration resetTokenExpire) {
        this.resetTokenExpire = resetTokenExpire;
    }

    public Duration getEmailTokenExpire() {
        return emailTokenExpire;
    }

    public void setEmailTokenExpire(Duration emailTokenExpire) {
        this.emailTokenExpire = emailTokenExpire;
    }

    public String getConsoleBaseUrl() {
        return consoleBaseUrl;
    }

    public void setConsoleBaseUrl(String consoleBaseUrl) {
        this.consoleBaseUrl = consoleBaseUrl;
    }
}

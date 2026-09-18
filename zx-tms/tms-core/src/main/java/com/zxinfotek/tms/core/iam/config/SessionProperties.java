package com.zxinfotek.tms.core.iam.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "tms.session")
public class SessionProperties {

    /** 滑动有效期 */
    private Duration idleTimeout = Duration.ofMinutes(30);

    /** 绝对有效期 */
    private Duration maxLifetime = Duration.ofHours(12);

    /** 最近活动时间回写数据库的节流间隔 */
    private Duration activeWriteBack = Duration.ofMinutes(1);

    public Duration getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(Duration idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public Duration getMaxLifetime() {
        return maxLifetime;
    }

    public void setMaxLifetime(Duration maxLifetime) {
        this.maxLifetime = maxLifetime;
    }

    public Duration getActiveWriteBack() {
        return activeWriteBack;
    }

    public void setActiveWriteBack(Duration activeWriteBack) {
        this.activeWriteBack = activeWriteBack;
    }
}

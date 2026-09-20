package com.zxinfotek.tms.infra.captcha;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "tms.captcha")
public class CaptchaProperties {

    /** 关闭后校验直接放行，仅供本地开发与自动化测试，生产必须保持开启 */
    private boolean enabled = true;

    private int length = 4;

    private Duration expire = Duration.ofMinutes(2);

    private int width = 150;

    private int height = 50;

    /** 字体序号，取值 1 至 10，对应 easy-captcha 内置字体 */
    private int font = 6;

    private float fontSize = 34f;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public Duration getExpire() {
        return expire;
    }

    public void setExpire(Duration expire) {
        this.expire = expire;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getFont() {
        return font;
    }

    public void setFont(int font) {
        this.font = font;
    }

    public float getFontSize() {
        return fontSize;
    }

    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
    }
}

package com.zxinfotek.tms.infra.captcha;

import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import com.zxinfotek.tms.infra.redis.RedisService;
import com.zxinfotek.tms.infra.security.TokenHasher;
import org.springframework.stereotype.Service;

/**
 * 图形验证码的签发与校验，答案只存 Redis 并随校验一次性消费（详细设计 3.1.5 第 12 至 14 条）。
 *
 * @author zxinfotek
 * @since 2026-09-20
 */
@Service
public class CaptchaService {

    private static final String KEY_PREFIX = "tms:captcha:";

    /** 易混淆字符，命中则重新生成，避免用户把 0 和 O、1 和 I 看错 */
    private static final String CONFUSABLE = "01OI";

    private static final int MAX_ATTEMPTS = 10;

    /** easy-captcha 内置字体套数，Captcha.FONT_n 常量按 0 起算，配置项按 1 起算的序号填写 */
    private static final int FONT_COUNT = 10;

    private final CaptchaProperties properties;
    private final RedisService redisService;

    public CaptchaService(CaptchaProperties properties, RedisService redisService) {
        this.properties = properties;
        this.redisService = redisService;
    }

    /**
     * 签发一张验证码，答案按标识存入 Redis 并在有效期后自动过期。
     *
     * @author zxinfotek
     * @since 2026-09-20
     */
    public CaptchaImage create() {
        Generated generated = generate();
        String id = TokenHasher.newToken();
        redisService.set(KEY_PREFIX + id, generated.text(), properties.getExpire());
        return new CaptchaImage(id, generated.image());
    }

    /**
     * 校验验证码，无论成败都先删除答案，防止同一标识被反复试答案。
     *
     * @author zxinfotek
     * @since 2026-09-20
     */
    public boolean verify(String id, String code) {
        if (!properties.isEnabled()) {
            return true;
        }
        if (id == null || id.isBlank() || code == null || code.isBlank()) {
            return false;
        }
        String key = KEY_PREFIX + id;
        String expected = redisService.get(key, String.class);
        redisService.delete(key);
        return expected != null && expected.equalsIgnoreCase(code.trim());
    }

    /** 出图与取文本，不涉及 Redis，便于单独验证生成规则 */
    Generated generate() {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            SpecCaptcha captcha = new SpecCaptcha(properties.getWidth(), properties.getHeight(),
                    properties.getLength());
            captcha.setCharType(Captcha.TYPE_NUM_AND_UPPER);
            int font = properties.getFont();
            if (font < 1 || font > FONT_COUNT) {
                throw new IllegalStateException("验证码字体序号应在 1 至 " + FONT_COUNT + " 之间，当前为 " + font);
            }
            try {
                captcha.setFont(font - 1, properties.getFontSize());
            } catch (Exception e) {
                throw new IllegalStateException("验证码字体加载失败，font=" + font, e);
            }
            String text = captcha.text();
            if (containsConfusable(text) && attempt < MAX_ATTEMPTS - 1) {
                continue;
            }
            return new Generated(text.toUpperCase(), captcha.toBase64());
        }
        throw new IllegalStateException("验证码生成失败");
    }

    private boolean containsConfusable(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (CONFUSABLE.indexOf(Character.toUpperCase(text.charAt(i))) >= 0) {
                return true;
            }
        }
        return false;
    }

    /** 验证码标识与 base64 图片，图片形如 data:image/png;base64,... */
    public record CaptchaImage(String id, String image) {
    }

    record Generated(String text, String image) {
    }
}

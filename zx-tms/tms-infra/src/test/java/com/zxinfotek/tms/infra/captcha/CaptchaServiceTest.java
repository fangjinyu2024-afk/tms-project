package com.zxinfotek.tms.infra.captcha;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 验证码生成规则校验：长度、字符集与出图尺寸，不依赖 Redis。
 *
 * @author zxinfotek
 * @since 2026-09-20
 */
class CaptchaServiceTest {

    private static final String PREFIX = "data:image/png;base64,";

    private final CaptchaProperties properties = new CaptchaProperties();
    private final CaptchaService service = new CaptchaService(properties, null);

    @Test
    @DisplayName("验证码文本符合长度与字符集，且不含易混淆字符")
    void textFollowsRules() {
        for (int i = 0; i < 50; i++) {
            String text = service.generate().text();
            assertEquals(properties.getLength(), text.length(), "验证码长度不符");
            assertTrue(text.matches("[A-Z0-9]+"), "验证码应只含大写字母与数字：" + text);
            assertTrue(text.chars().noneMatch(c -> "01OI".indexOf(c) >= 0),
                    "验证码不应包含易混淆字符：" + text);
        }
    }

    @Test
    @DisplayName("出图为合法 PNG 且尺寸与配置一致")
    void imageIsPngOfConfiguredSize() throws IOException {
        String image = service.generate().image();
        assertTrue(image.startsWith(PREFIX), "图片应为 base64 data URI：" + image.substring(0, 32));
        byte[] bytes = Base64.getDecoder().decode(image.substring(PREFIX.length()));
        BufferedImage png = ImageIO.read(new ByteArrayInputStream(bytes));
        assertNotNull(png, "图片内容不是合法 PNG");
        assertEquals(properties.getWidth(), png.getWidth());
        assertEquals(properties.getHeight(), png.getHeight());
    }

    @Test
    @DisplayName("字体序号按 1 起算，越界时报错而不是静默用错字体")
    void fontIndexIsOneBased() {
        properties.setFont(0);
        assertThrows(IllegalStateException.class, service::generate);
        properties.setFont(11);
        assertThrows(IllegalStateException.class, service::generate);
        properties.setFont(10);
        assertNotNull(service.generate().image());
        properties.setFont(new CaptchaProperties().getFont());
    }

    @Test
    @DisplayName("关闭开关后校验直接放行")
    void disabledSkipsVerification() {
        properties.setEnabled(false);
        assertTrue(service.verify(null, null));
        properties.setEnabled(true);
    }
}

package com.zxinfotek.tms.admin.i18n;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 管理后台消息资源校验：中英文键必须一一对应（详细设计 7.13）。
 *
 * @author zxinfotek
 * @since 2026-09-20
 */
class AdminMessageBundleTest {

    @Test
    @DisplayName("中英文消息资源的键一一对应")
    void bundlesHaveSameKeys() throws IOException {
        Set<String> zh = new TreeSet<>(load("i18n/admin_zh_CN.properties").stringPropertyNames());
        Set<String> en = new TreeSet<>(load("i18n/admin_en.properties").stringPropertyNames());
        assertEquals(zh, en, "admin 中英文键不一致");
        assertTrue(!zh.isEmpty(), "未读到 admin 消息资源");
    }

    private Properties load(String resource) throws IOException {
        Properties properties = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertTrue(in != null, "找不到消息资源：" + resource);
            properties.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
        return properties;
    }
}

package com.zxinfotek.tms.core.i18n;

import com.zxinfotek.tms.common.exception.CommonErrorCode;
import com.zxinfotek.tms.common.exception.ErrorCode;
import com.zxinfotek.tms.common.exception.PermErrorCode;
import com.zxinfotek.tms.core.iam.IamErrorCode;
import com.zxinfotek.tms.core.iam.constant.PermissionCatalog;
import com.zxinfotek.tms.core.product.ProductErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 消息资源校验：中英文键必须一一对应，错误码与权限目录必须有对应文案。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
class MessageBundleTest {

    private static final List<String> BUNDLES = List.of("i18n/common", "i18n/core");

    private static final Pattern VALIDATION_MESSAGE = Pattern.compile("message = \"([^\"]+)\"");

    @Test
    @DisplayName("中英文消息资源的键一一对应")
    void bundlesHaveSameKeys() throws IOException {
        for (String bundle : BUNDLES) {
            Set<String> zh = new TreeSet<>(load(bundle + "_zh_CN.properties").stringPropertyNames());
            Set<String> en = new TreeSet<>(load(bundle + "_en.properties").stringPropertyNames());
            assertEquals(zh, en, bundle + " 中英文键不一致");
        }
    }

    @Test
    @DisplayName("每个错误码都有中英文文案")
    void errorCodesHaveMessages() throws IOException {
        List<ErrorCode> codes = new ArrayList<>();
        codes.addAll(List.of(CommonErrorCode.values()));
        codes.addAll(List.of(PermErrorCode.values()));
        codes.addAll(List.of(IamErrorCode.values()));
        codes.addAll(List.of(ProductErrorCode.values()));

        Properties zh = merged("_zh_CN.properties");
        Properties en = merged("_en.properties");
        for (ErrorCode code : codes) {
            assertTrue(zh.containsKey(code.getMessageKey()), "缺少中文文案：" + code.getMessageKey());
            assertTrue(en.containsKey(code.getMessageKey()), "缺少英文文案：" + code.getMessageKey());
        }
    }

    @Test
    @DisplayName("权限目录的菜单、分组与操作都有中英文名称")
    void permissionCatalogHasMessages() throws IOException {
        Properties zh = merged("_zh_CN.properties");
        Properties en = merged("_en.properties");
        for (PermissionCatalog.MenuDefinition menu : PermissionCatalog.menus()) {
            assertTrue(zh.containsKey("permission.menu." + menu.menuKey()), "缺少菜单中文名：" + menu.menuKey());
            assertTrue(en.containsKey("permission.menu." + menu.menuKey()), "缺少菜单英文名：" + menu.menuKey());
            assertTrue(zh.containsKey("permission.group." + menu.group().groupKey()));
            assertTrue(en.containsKey("permission.group." + menu.group().groupKey()));
        }
        Set<String> actions = PermissionCatalog.permissions().stream()
                .map(PermissionCatalog.PermissionDefinition::action).collect(Collectors.toSet());
        for (String action : actions) {
            assertTrue(zh.containsKey("permission.action." + action), "缺少操作中文名：" + action);
            assertTrue(en.containsKey("permission.action." + action), "缺少操作英文名：" + action);
        }
    }

    @Test
    @DisplayName("请求参数校验注解的消息键都有中英文文案")
    void validationMessagesHaveTexts() throws IOException {
        Properties zh = merged("_zh_CN.properties");
        Properties en = merged("_en.properties");
        Set<String> keys = new TreeSet<>();
        Path sources = Path.of("src/main/java");
        try (Stream<Path> files = Files.walk(sources)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
                Matcher matcher = VALIDATION_MESSAGE.matcher(Files.readString(file, StandardCharsets.UTF_8));
                while (matcher.find()) {
                    keys.add(matcher.group(1));
                }
            }
        }
        assertTrue(!keys.isEmpty(), "未扫描到校验消息键");
        for (String key : keys) {
            assertTrue(zh.containsKey(key), "缺少中文文案：" + key);
            assertTrue(en.containsKey(key), "缺少英文文案：" + key);
        }
    }

    private Properties merged(String suffix) throws IOException {
        Properties properties = new Properties();
        for (String bundle : BUNDLES) {
            properties.putAll(load(bundle + suffix));
        }
        return properties;
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

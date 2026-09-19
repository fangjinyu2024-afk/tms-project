package com.zxinfotek.tms.infra.i18n;

import com.zxinfotek.tms.common.enums.Labeled;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * 消息国际化入口：按当前请求语言解析错误提示、枚举名称与导出表头，解析不到时回落到中文默认值。
 *
 * <p>语言由 {@code Accept-Language} 请求头决定，支持 zh-CN 与 en，默认 zh-CN。</p>
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
@Component
public class I18nMessages {

    private static MessageSource messageSource;

    public I18nMessages(MessageSource messageSource) {
        I18nMessages.messageSource = messageSource;
    }

    public static Locale currentLocale() {
        return LocaleContextHolder.getLocale();
    }

    /** 按键解析消息，未配置该键时返回键本身，便于发现遗漏 */
    public static String get(String key, Object... args) {
        return getOrDefault(key, key, args);
    }

    public static String getOrDefault(String key, String fallback, Object... args) {
        if (key == null) {
            return fallback;
        }
        if (messageSource == null) {
            return fallback;
        }
        try {
            return messageSource.getMessage(key, args, currentLocale());
        } catch (NoSuchMessageException e) {
            return fallback;
        }
    }

    /**
     * 解析枚举名称，键为 {@code enum.枚举类名.取值}，解析不到时回落到枚举自带的中文名称。
     *
     * @author zxinfotek
     * @since 2026-09-18
     */
    public static <E extends Enum<E> & Labeled> String label(E value) {
        if (value == null) {
            return null;
        }
        return getOrDefault("enum." + value.getClass().getSimpleName() + "." + value.name(), value.getLabel());
    }
}

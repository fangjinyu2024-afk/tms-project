package com.zxinfotek.tms.infra.i18n;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

/**
 * 语言解析：优先取 {@code lang} 请求参数，其次取 Accept-Language，仅支持中文与英文，默认中文。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
public class TmsLocaleResolver extends AcceptHeaderLocaleResolver {

    public static final Locale DEFAULT_LOCALE = Locale.SIMPLIFIED_CHINESE;
    public static final List<Locale> SUPPORTED = List.of(Locale.SIMPLIFIED_CHINESE, Locale.ENGLISH);

    public TmsLocaleResolver() {
        setDefaultLocale(DEFAULT_LOCALE);
        setSupportedLocales(SUPPORTED);
    }

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        Locale fromParameter = fromLanguageTag(request.getParameter("lang"));
        return fromParameter != null ? fromParameter : super.resolveLocale(request);
    }

    /** 过滤为受支持语言，英文以外一律回落中文 */
    public static Locale fromLanguageTag(String languageTag) {
        if (languageTag == null || languageTag.isBlank()) {
            return null;
        }
        Locale locale = Locale.forLanguageTag(languageTag.trim());
        if (locale.getLanguage().isEmpty()) {
            return null;
        }
        return Locale.ENGLISH.getLanguage().equals(locale.getLanguage()) ? Locale.ENGLISH : DEFAULT_LOCALE;
    }

    /** 过滤器阶段（早于 DispatcherServlet）解析语言，使鉴权失败等响应同样按语言返回 */
    public static Locale resolveEarly(HttpServletRequest request) {
        Locale locale = fromLanguageTag(request.getParameter("lang"));
        if (locale != null) {
            return locale;
        }
        String header = request.getHeader("Accept-Language");
        if (header == null || header.isBlank()) {
            return DEFAULT_LOCALE;
        }
        for (String part : header.split(",")) {
            Locale candidate = fromLanguageTag(part.split(";")[0]);
            if (candidate != null) {
                return candidate;
            }
        }
        return DEFAULT_LOCALE;
    }
}

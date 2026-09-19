package com.zxinfotek.tms.infra.i18n;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * 消息资源配置：各模块在自己的 {@code i18n/} 目录下提供 zh_CN 与 en 两份文案，此处按模块合并。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
@Configuration
public class MessageSourceConfig {

    @Bean
    public MessageSource messageSource(
            @Value("${tms.i18n.basenames:classpath:i18n/common,classpath:i18n/infra,classpath:i18n/core,classpath:i18n/admin}")
            String[] basenames) {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames(basenames);
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setUseCodeAsDefaultMessage(false);
        return messageSource;
    }
}

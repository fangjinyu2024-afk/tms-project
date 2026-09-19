package com.zxinfotek.tms.admin.config;

import com.zxinfotek.tms.infra.i18n.TmsLocaleResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;

@Configuration
public class I18nConfig {

    @Bean
    public LocaleResolver localeResolver() {
        return new TmsLocaleResolver();
    }
}

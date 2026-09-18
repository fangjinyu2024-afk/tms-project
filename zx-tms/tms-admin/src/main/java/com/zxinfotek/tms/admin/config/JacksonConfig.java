package com.zxinfotek.tms.admin.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.zxinfotek.tms.common.util.UtcTimes;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * 接口序列化约定：时间统一 ISO-8601 UTC 格式（详细设计 7.9）；雪花主键以字符串输出，
 * 避免超出 JavaScript 安全整数范围后精度丢失（详细设计 5.1）。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer utcDateTimeCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule();
            module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(UtcTimes.ISO_UTC));
            module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(UtcTimes.ISO_UTC));
            module.addSerializer(Long.class, ToStringSerializer.instance);
            module.addSerializer(Long.TYPE, ToStringSerializer.instance);
            builder.modules(module);
        };
    }
}

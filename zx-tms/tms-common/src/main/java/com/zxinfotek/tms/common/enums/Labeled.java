package com.zxinfotek.tms.common.enums;

/**
 * 带编码与名称的枚举契约，名称为中文默认值，接口响应按请求语言解析后覆盖。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
public interface Labeled {

    String getCode();

    String getLabel();
}

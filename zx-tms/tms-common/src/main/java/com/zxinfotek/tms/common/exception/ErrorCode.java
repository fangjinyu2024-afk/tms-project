package com.zxinfotek.tms.common.exception;

/**
 * 错误码契约，各模块以枚举实现，编码格式为「模块前缀_三位序号」。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
public interface ErrorCode {

    String getCode();

    /** 默认中文消息，接口响应按请求语言经消息资源解析，解析不到时回落到本值 */
    String getMessage();

    /** 消息资源键，默认取 {@code error.错误码} */
    default String getMessageKey() {
        return "error." + getCode();
    }
}

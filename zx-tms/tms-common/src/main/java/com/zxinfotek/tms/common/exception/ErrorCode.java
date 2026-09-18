package com.zxinfotek.tms.common.exception;

/**
 * 错误码契约，各模块以枚举实现，编码格式为「模块前缀_三位序号」。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
public interface ErrorCode {

    String getCode();

    String getMessage();
}

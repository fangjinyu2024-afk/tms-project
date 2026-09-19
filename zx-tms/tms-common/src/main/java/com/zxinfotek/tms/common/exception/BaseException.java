package com.zxinfotek.tms.common.exception;

/**
 * 业务异常基类：携带错误码、消息资源键与参数，具体文案在接口层按请求语言解析（详细设计 7.4）。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
public abstract class BaseException extends RuntimeException {

    private final transient ErrorCode errorCode;
    private final transient String messageKey;
    private final transient Object[] messageArgs;
    private transient Object detail;

    protected BaseException(ErrorCode errorCode) {
        this(errorCode, null);
    }

    protected BaseException(ErrorCode errorCode, String messageKey, Object... messageArgs) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.messageKey = messageKey;
        this.messageArgs = messageArgs;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /** 优先使用异常自带的消息键，未指定时取错误码默认键 */
    public String resolveMessageKey() {
        return messageKey == null ? errorCode.getMessageKey() : messageKey;
    }

    public Object[] getMessageArgs() {
        return messageArgs == null ? new Object[0] : messageArgs;
    }

    public Object getDetail() {
        return detail;
    }

    protected void detail(Object detail) {
        this.detail = detail;
    }
}

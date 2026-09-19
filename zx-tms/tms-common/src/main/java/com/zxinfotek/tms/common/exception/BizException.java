package com.zxinfotek.tms.common.exception;

/** 业务校验失败，HTTP 400；依赖不可用（COMMON_004）由接口层映射为 503。 */
public class BizException extends BaseException {

    public BizException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BizException(ErrorCode errorCode, String messageKey, Object... messageArgs) {
        super(errorCode, messageKey, messageArgs);
    }

    /** 附带返回给前端的明细，如越权的权限码清单 */
    public BizException withDetail(Object detail) {
        detail(detail);
        return this;
    }
}

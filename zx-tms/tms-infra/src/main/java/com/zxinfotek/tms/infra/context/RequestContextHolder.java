package com.zxinfotek.tms.infra.context;

/**
 * 请求上下文持有者，Service 方法从此处取登录主体，不接收 HttpServletRequest。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
public final class RequestContextHolder {

    private static final ThreadLocal<RequestContext> HOLDER = new ThreadLocal<>();

    private RequestContextHolder() {
    }

    public static void set(RequestContext context) {
        HOLDER.set(context);
    }

    public static RequestContext get() {
        RequestContext context = HOLDER.get();
        if (context == null) {
            context = new RequestContext();
            HOLDER.set(context);
        }
        return context;
    }

    public static RequestContext peek() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}

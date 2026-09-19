package com.zxinfotek.tms.admin.aop;

import com.zxinfotek.tms.common.enums.DataScope;
import com.zxinfotek.tms.common.exception.AuthException;
import com.zxinfotek.tms.common.exception.PermErrorCode;
import com.zxinfotek.tms.common.exception.PermissionException;
import com.zxinfotek.tms.core.iam.IamErrorCode;
import com.zxinfotek.tms.infra.context.RequestContext;
import com.zxinfotek.tms.infra.context.RequestContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 功能权限与数据范围校验：命中权限码后把该权限码对应的数据范围写入请求上下文供 SQL 拦截器使用。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
@Aspect
@Component
@Order(10)
public class PermissionAspect {

    @Around("@annotation(requiresPerm)")
    public Object around(ProceedingJoinPoint joinPoint, RequiresPerm requiresPerm) throws Throwable {
        RequestContext context = RequestContextHolder.get();
        if (!context.isAuthenticated()) {
            throw new AuthException(IamErrorCode.AUTH_004);
        }
        DataScope scope = resolveScope(context, requiresPerm);
        DataScope previous = context.getDataScope();
        context.setDataScope(scope);
        try {
            return joinPoint.proceed();
        } finally {
            context.setDataScope(previous);
        }
    }

    /** 单权限码直接取其数据范围；多权限码任一命中即可，数据范围取命中项中的较大者。 */
    private DataScope resolveScope(RequestContext context, RequiresPerm requiresPerm) {
        if (!requiresPerm.value().isEmpty()) {
            DataScope scope = context.scopeOf(requiresPerm.value());
            if (scope == null) {
                throw new PermissionException(PermErrorCode.PERM_001, "msg.web.noPermissionCode",
                        requiresPerm.value());
            }
            return scope;
        }
        DataScope matched = null;
        for (String permCode : requiresPerm.anyOf()) {
            matched = DataScope.max(matched, context.scopeOf(permCode));
        }
        if (matched == null) {
            throw new PermissionException(PermErrorCode.PERM_001);
        }
        return matched;
    }
}

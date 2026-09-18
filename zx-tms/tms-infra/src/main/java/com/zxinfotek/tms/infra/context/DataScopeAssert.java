package com.zxinfotek.tms.infra.context;

import com.zxinfotek.tms.common.enums.DataScope;
import com.zxinfotek.tms.common.exception.PermErrorCode;
import com.zxinfotek.tms.common.exception.PermissionException;

/**
 * 单条记录的数据范围校验：按 ID 操作时除 SQL 过滤外再校验记录归属（详细设计 7.1）。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
public final class DataScopeAssert {

    private DataScopeAssert() {
    }

    /**
     * 校验目标记录的租户与机构路径是否在当前成员的可访问范围内，超出范围抛 PERM_002。
     *
     * @author zxinfotek
     * @since 2026-09-18
     */
    public static void within(Long targetTenantId, String targetOrgPath) {
        RequestContext context = RequestContextHolder.get();
        if (!context.isAuthenticated()) {
            throw new PermissionException(PermErrorCode.PERM_002);
        }
        if (!context.isPlatform() && targetTenantId != null
                && !targetTenantId.equals(context.getTenantId())) {
            throw new PermissionException(PermErrorCode.PERM_002);
        }
        DataScope scope = context.getDataScope();
        if (scope == null || targetOrgPath == null) {
            return;
        }
        String memberPath = context.getOrgPath();
        if (memberPath == null) {
            throw new PermissionException(PermErrorCode.PERM_002);
        }
        boolean allowed = scope == DataScope.SELF_ORG
                ? memberPath.equals(targetOrgPath)
                : targetOrgPath.startsWith(memberPath);
        if (!allowed) {
            throw new PermissionException(PermErrorCode.PERM_002);
        }
    }

    /** 校验目标机构是否在当前成员可管理范围内（不依赖记录本身的租户字段）。 */
    public static void orgWithin(String targetOrgPath) {
        within(null, targetOrgPath);
    }
}

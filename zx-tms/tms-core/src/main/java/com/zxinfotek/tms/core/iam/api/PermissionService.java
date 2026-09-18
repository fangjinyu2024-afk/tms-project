package com.zxinfotek.tms.core.iam.api;

import com.zxinfotek.tms.common.enums.DataScope;
import com.zxinfotek.tms.core.iam.api.model.PermissionCatalogVO;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface PermissionService {

    /** 服务启动时把代码常量中的权限目录同步到 t_permission。 */
    void syncCatalog();

    /** 计算成员的有效权限，结构为「权限码 → 数据范围」。 */
    EffectivePermissions load(Long memberId);

    /** 按当前权限版本判断会话中的权限是否已过期。 */
    boolean versionChanged(Long memberId, Integer featureVersion, Integer memberPermVersion,
                           Integer rolePermVersion);

    List<PermissionCatalogVO> catalog(Long orgId);

    /** 操作者对目标机构可授予的权限码集合。 */
    Set<String> grantablePermCodes(Long orgId);

    Set<String> tenantMenuKeys(Long tenantId);

    record EffectivePermissions(Map<String, DataScope> permissions,
                               Integer featureVersion,
                               Integer memberPermVersion,
                               Integer rolePermVersion) {
    }
}

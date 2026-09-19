package com.zxinfotek.tms.core.iam.api;

import com.zxinfotek.tms.common.model.PageResult;
import com.zxinfotek.tms.core.iam.api.model.RoleOptionVO;
import com.zxinfotek.tms.core.iam.api.model.RoleQuery;
import com.zxinfotek.tms.core.iam.api.model.RoleSaveRequest;
import com.zxinfotek.tms.core.iam.api.model.RoleVO;
import com.zxinfotek.tms.core.iam.api.model.StatusRequest;
import com.zxinfotek.tms.core.iam.enums.BuiltinRole;

import java.util.List;

public interface RoleService {

    PageResult<RoleVO> page(RoleQuery query);

    RoleVO detail(Long id);

    Long create(RoleSaveRequest request);

    void update(Long id, RoleSaveRequest request);

    Long copy(Long id);

    void changeStatus(Long id, StatusRequest request);

    void delete(Long id);

    List<RoleOptionVO> assignableOptions(Long orgId);

    String export(RoleQuery query);

    /** 确保租户的内置角色存在并与权限目录保持一致，返回内置角色 ID。 */
    Long ensureBuiltinRole(Long tenantId, Long ownerOrgId, String ownerOrgPath, BuiltinRole builtinRole);

    /** 按权限目录重算全部已存在的内置角色权限码，返回发生变更的角色数。 */
    int syncBuiltinRolePermissions();

    /** 校验目标角色是否可由当前操作者分配给目标机构的成员。 */
    void assertAssignable(List<Long> roleIds, Long tenantId, String targetOrgPath);
}

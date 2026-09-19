package com.zxinfotek.tms.core.iam.enums;

import com.zxinfotek.tms.core.iam.constant.MenuKeys;
import com.zxinfotek.tms.core.iam.constant.PermissionCatalog;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 内置角色及其权限构成：内置管理员角色取所在账户边界内的全部权限码，
 * 权限目录新增权限码时自动并入（详细设计 3.2.5 第 10、14 条）。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
public enum BuiltinRole {

    PLATFORM_ADMIN("平台管理员", "平台内置管理员，持有全部平台权限"),
    TENANT_ADMIN("客户管理员", "客户内置管理员，持有客户边界内全部权限"),
    BRANCH_ADMIN("机构管理员", "机构内置管理员，不含角色维护权限");

    /** 客户侧内置角色不含的菜单：客户人员操作支付密钥须由自定义角色显式授予 */
    private static final Set<String> TENANT_EXCLUDED_MENUS =
            Set.of(MenuKeys.KEYS, MenuKeys.RKI, MenuKeys.RKI_RECORDS);

    /** 机构管理员在客户管理员基础上再去掉的菜单：角色目录由客户管理员维护 */
    private static final Set<String> BRANCH_EXCLUDED_MENUS = Set.of(MenuKeys.ROLES);

    private final String roleName;
    private final String description;

    BuiltinRole(String roleName, String description) {
        this.roleName = roleName;
        this.description = description;
    }

    /**
     * 计算该内置角色的权限码：平台管理员取权限目录全部权限码；客户侧内置角色取非平台专属权限码
     * 并去除支付密钥相关菜单，机构管理员再去除角色菜单。
     *
     * @author zxinfotek
     * @since 2026-09-18
     */
    public List<String> permCodes() {
        return PermissionCatalog.permissions().stream()
                .filter(permission -> this == PLATFORM_ADMIN || !permission.platformOnly())
                .filter(permission -> this == PLATFORM_ADMIN
                        || !TENANT_EXCLUDED_MENUS.contains(permission.menuKey()))
                .filter(permission -> this != BRANCH_ADMIN
                        || !BRANCH_EXCLUDED_MENUS.contains(permission.menuKey()))
                .map(PermissionCatalog.PermissionDefinition::permCode)
                .collect(Collectors.toList());
    }

    public String getRoleName() {
        return roleName;
    }

    public String getDescription() {
        return description;
    }
}

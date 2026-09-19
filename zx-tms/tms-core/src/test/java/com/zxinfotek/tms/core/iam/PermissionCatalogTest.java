package com.zxinfotek.tms.core.iam;

import com.zxinfotek.tms.core.iam.constant.MenuKeys;
import com.zxinfotek.tms.core.iam.constant.PermissionCatalog;
import com.zxinfotek.tms.core.iam.enums.BuiltinRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 权限目录数量口径与平台专属标注的校验，对应详细设计 6.2.1 与 6.2.2。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
class PermissionCatalogTest {

    @Test
    @DisplayName("权限目录为 25 个菜单、120 个权限码")
    void catalogCounts() {
        assertEquals(25, PermissionCatalog.menus().size());
        assertEquals(120, PermissionCatalog.permissions().size());
    }

    @Test
    @DisplayName("客户功能授权可选菜单为 20 个，且不含平台专属菜单")
    void tenantSelectableMenus() {
        List<String> selectable = PermissionCatalog.tenantSelectableMenuKeys();
        assertEquals(20, selectable.size());
        assertTrue(selectable.contains(MenuKeys.HOME));
        assertTrue(selectable.contains(MenuKeys.DEVICES));
        assertFalse(selectable.contains(MenuKeys.CUSTOMERS));
        assertFalse(selectable.contains(MenuKeys.PRODUCTS));
        assertFalse(selectable.contains(MenuKeys.MODE));
        assertFalse(selectable.contains(MenuKeys.CAS));
        assertFalse(selectable.contains(MenuKeys.SERVER_CERTS));
    }

    @Test
    @DisplayName("平台专属是权限码级：设备菜单仅入库、出库、退货回收三项为平台专属")
    void platformOnlyIsPermissionLevel() {
        assertTrue(PermissionCatalog.isPlatformOnly("devices:stockin"));
        assertTrue(PermissionCatalog.isPlatformOnly("devices:stockout"));
        assertTrue(PermissionCatalog.isPlatformOnly("devices:return"));
        assertFalse(PermissionCatalog.isPlatformOnly("devices:view"));
        assertFalse(PermissionCatalog.isPlatformOnly("devices:transfer"));
        assertFalse(PermissionCatalog.isPlatformOnly("devices:scrap"));
        assertFalse(PermissionCatalog.isPlatformOnly("grants:create"));
        assertFalse(PermissionCatalog.isPlatformOnly("codes:create"));
    }

    @Test
    @DisplayName("内置平台管理员持有全部权限码")
    void platformAdminHoldsAllPermissions() {
        assertEquals(PermissionCatalog.permissions().size(), BuiltinRole.PLATFORM_ADMIN.permCodes().size());
        Set<String> menus = BuiltinRole.PLATFORM_ADMIN.permCodes().stream()
                .map(code -> code.substring(0, code.indexOf(':'))).collect(Collectors.toSet());
        assertTrue(menus.contains(MenuKeys.KEYS));
        assertTrue(menus.contains(MenuKeys.RKI));
        assertTrue(menus.contains(MenuKeys.RKI_RECORDS));
    }

    @Test
    @DisplayName("内置机构管理员不含角色维护权限")
    void branchAdminExcludesRoleMenu() {
        Set<String> menus = BuiltinRole.BRANCH_ADMIN.permCodes().stream()
                .map(code -> code.substring(0, code.indexOf(':'))).collect(Collectors.toSet());
        assertFalse(menus.contains(MenuKeys.ROLES), "机构管理员不应包含角色管理");
        assertTrue(menus.contains(MenuKeys.MEMBERS));
    }

    @Test
    @DisplayName("客户侧内置角色不含平台专属权限码与支付密钥菜单")
    void tenantBuiltinRolesExcludePlatformOnly() {
        for (BuiltinRole role : List.of(BuiltinRole.TENANT_ADMIN, BuiltinRole.BRANCH_ADMIN)) {
            assertTrue(role.permCodes().stream().noneMatch(PermissionCatalog::isPlatformOnly),
                    role + " 不应包含平台专属权限码");
            Set<String> menus = role.permCodes().stream()
                    .map(code -> code.substring(0, code.indexOf(':'))).collect(Collectors.toSet());
            assertFalse(menus.contains(MenuKeys.KEYS), role + " 不应包含密钥管理");
            assertFalse(menus.contains(MenuKeys.RKI), role + " 不应包含 RKI 任务");
            assertFalse(menus.contains(MenuKeys.RKI_RECORDS), role + " 不应包含 RKI 执行记录");
        }
        assertTrue(BuiltinRole.PLATFORM_ADMIN.permCodes().contains("customers:create"));
        assertEquals(71, BuiltinRole.TENANT_ADMIN.permCodes().size());
        assertEquals(64, BuiltinRole.BRANCH_ADMIN.permCodes().size());
    }
}

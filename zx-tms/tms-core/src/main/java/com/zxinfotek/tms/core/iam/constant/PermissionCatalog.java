package com.zxinfotek.tms.core.iam.constant;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 系统权限目录常量，25 个菜单、120 个权限码，服务启动时同步到 t_permission（详细设计 6.2.2）。
 *
 * <p>菜单与操作对平台／客户的适用性由 {@code platformOnly} 承载，粒度为权限码而非菜单，
 * 代码中不再维护硬编码的平台专属菜单名单。</p>
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
public final class PermissionCatalog {

    public static final String GROUP_HOME = "工作台";
    public static final String GROUP_DEVICE = "设备管理";
    public static final String GROUP_MAINTAIN = "远程维护";
    public static final String GROUP_ACTIVATION = "激活与证书";
    public static final String GROUP_SYSTEM = "系统管理";

    /** 操作编码与名称，详细设计 6.2.2。 */
    public static final Map<String, String> ACTION_NAMES = actionNames();

    private static final List<MenuDefinition> MENUS = buildMenus();

    private static final List<PermissionDefinition> PERMISSIONS = flatten(MENUS);

    private static final Map<String, PermissionDefinition> INDEX = PERMISSIONS.stream()
            .collect(Collectors.toMap(PermissionDefinition::permCode, definition -> definition));

    private PermissionCatalog() {
    }

    public static List<MenuDefinition> menus() {
        return MENUS;
    }

    public static List<PermissionDefinition> permissions() {
        return PERMISSIONS;
    }

    /**
     * 客户功能授权可选菜单：权限目录中存在至少一个非平台专属权限码的菜单（详细设计 3.3.5 第 6 条）。
     *
     * @author zxinfotek
     * @since 2026-09-18
     */
    public static List<String> tenantSelectableMenuKeys() {
        return MENUS.stream()
                .filter(menu -> menu.actions().stream().anyMatch(action -> !action.platformOnly()))
                .map(MenuDefinition::menuKey)
                .collect(Collectors.toList());
    }

    public static List<String> allMenuKeys() {
        return MENUS.stream().map(MenuDefinition::menuKey).collect(Collectors.toList());
    }

    public static boolean isPlatformOnly(String permCode) {
        PermissionDefinition definition = INDEX.get(permCode);
        return definition != null && definition.platformOnly();
    }

    public static boolean exists(String permCode) {
        return INDEX.containsKey(permCode);
    }

    public static PermissionDefinition find(String permCode) {
        return INDEX.get(permCode);
    }

    private static List<PermissionDefinition> flatten(List<MenuDefinition> menus) {
        List<PermissionDefinition> permissions = new ArrayList<>();
        int sort = 0;
        for (MenuDefinition menu : menus) {
            for (ActionDefinition action : menu.actions()) {
                permissions.add(new PermissionDefinition(
                        menu.menuKey() + ":" + action.action(),
                        menu.menuKey(),
                        menu.menuName(),
                        menu.groupName(),
                        action.action(),
                        ACTION_NAMES.getOrDefault(action.action(), action.action()),
                        action.platformOnly(),
                        ++sort));
            }
        }
        return List.copyOf(permissions);
    }

    private static List<MenuDefinition> buildMenus() {
        List<MenuDefinition> menus = new ArrayList<>();
        menus.add(menu(MenuKeys.HOME, "工作台", GROUP_HOME, Set.of(), "view"));
        menus.add(menu(MenuKeys.DEVICES, "设备列表", GROUP_DEVICE,
                Set.of("stockin", "stockout", "return"),
                "view", "stockin", "stockout", "transfer", "return", "scrap", "export"));
        menus.add(menu(MenuKeys.GROUPS, "设备分组", GROUP_DEVICE, Set.of(),
                "view", "create", "edit", "delete", "import", "export"));
        menus.add(menu(MenuKeys.OPERATIONS, "流转记录", GROUP_DEVICE, Set.of(),
                "view", "revoke", "export"));
        menus.add(platformMenu(MenuKeys.MODE, "设备模式任务", GROUP_DEVICE,
                "view", "create", "edit", "publish", "stop", "copy", "delete", "export"));
        menus.add(menu(MenuKeys.PACKAGES, "升级包管理", GROUP_MAINTAIN, Set.of(),
                "view", "create", "edit", "upload", "toggle", "delete", "export"));
        menus.add(menu(MenuKeys.OTA, "OTA 任务", GROUP_MAINTAIN, Set.of(),
                "view", "create", "edit", "publish", "stop", "copy", "delete", "export"));
        menus.add(menu(MenuKeys.OTA_RECORDS, "OTA 执行记录", GROUP_MAINTAIN, Set.of(), "view", "export"));
        menus.add(menu(MenuKeys.KEYS, "密钥管理", GROUP_MAINTAIN, Set.of(),
                "view", "import", "edit", "toggle", "export"));
        menus.add(menu(MenuKeys.RKI, "RKI 任务", GROUP_MAINTAIN, Set.of(),
                "view", "create", "edit", "publish", "stop", "copy", "delete", "export"));
        menus.add(menu(MenuKeys.RKI_RECORDS, "RKI 执行记录", GROUP_MAINTAIN, Set.of(), "view", "export"));
        menus.add(menu(MenuKeys.GRANTS, "激活授权", GROUP_ACTIVATION, Set.of(),
                "view", "create", "edit", "toggle", "export"));
        menus.add(menu(MenuKeys.CODES, "授权码记录", GROUP_ACTIVATION, Set.of(),
                "view", "create", "revoke", "export"));
        menus.add(menu(MenuKeys.ACTIVATIONS, "设备激活记录", GROUP_ACTIVATION, Set.of(), "view", "export"));
        menus.add(platformMenu(MenuKeys.CAS, "CA 管理", GROUP_ACTIVATION,
                "view", "generate", "import", "toggle", "provision", "export"));
        menus.add(menu(MenuKeys.CERTS, "设备证书", GROUP_ACTIVATION, Set.of(), "view", "export"));
        menus.add(platformMenu(MenuKeys.SERVER_CERTS, "平台服务证书", GROUP_ACTIVATION,
                "view", "sign", "import", "toggle", "export"));
        menus.add(platformMenu(MenuKeys.CUSTOMERS, "客户管理", GROUP_SYSTEM,
                "view", "create", "edit", "toggle", "delete", "authorize", "export"));
        menus.add(menu(MenuKeys.ORGS, "机构管理", GROUP_SYSTEM, Set.of(),
                "view", "create", "edit", "toggle", "delete", "export"));
        menus.add(menu(MenuKeys.MEMBERS, "成员管理", GROUP_SYSTEM, Set.of(),
                "view", "create", "edit", "toggle", "delete", "reset", "assign", "export"));
        menus.add(menu(MenuKeys.ROLES, "角色管理", GROUP_SYSTEM, Set.of(),
                "view", "create", "edit", "toggle", "delete", "copy", "export"));
        menus.add(platformMenu(MenuKeys.PRODUCTS, "产品与型号", GROUP_SYSTEM,
                "view", "create", "edit", "delete", "export"));
        menus.add(menu(MenuKeys.LOGS, "操作日志", GROUP_SYSTEM, Set.of(), "view", "export"));
        menus.add(menu(MenuKeys.LOGINS, "登录日志", GROUP_SYSTEM, Set.of(), "view", "export"));
        menus.add(menu(MenuKeys.SESSIONS, "在线会话", GROUP_SYSTEM, Set.of(), "view", "force"));
        return List.copyOf(menus);
    }

    private static MenuDefinition menu(String menuKey, String menuName, String groupName,
                                       Set<String> platformOnlyActions, String... actions) {
        List<ActionDefinition> definitions = new ArrayList<>();
        for (String action : actions) {
            definitions.add(new ActionDefinition(action, platformOnlyActions.contains(action)));
        }
        return new MenuDefinition(menuKey, menuName, groupName, List.copyOf(definitions));
    }

    private static MenuDefinition platformMenu(String menuKey, String menuName, String groupName,
                                               String... actions) {
        List<ActionDefinition> definitions = new ArrayList<>();
        for (String action : actions) {
            definitions.add(new ActionDefinition(action, true));
        }
        return new MenuDefinition(menuKey, menuName, groupName, List.copyOf(definitions));
    }

    private static Map<String, String> actionNames() {
        Map<String, String> names = new LinkedHashMap<>();
        names.put("view", "查看");
        names.put("create", "新增");
        names.put("edit", "编辑");
        names.put("delete", "删除");
        names.put("export", "导出");
        names.put("import", "导入");
        names.put("toggle", "启用／停用");
        names.put("copy", "复制");
        names.put("publish", "发布");
        names.put("stop", "停止");
        names.put("upload", "上传版本");
        names.put("revoke", "撤销");
        names.put("reset", "重置密码");
        names.put("assign", "分配角色");
        names.put("authorize", "功能授权");
        names.put("force", "强制下线");
        names.put("generate", "自签生成");
        names.put("provision", "标记产线预置");
        names.put("sign", "平台签发");
        names.put("stockin", "入库");
        names.put("stockout", "出库");
        names.put("transfer", "转移");
        names.put("return", "退货回收");
        names.put("scrap", "报废");
        return Map.copyOf(names);
    }

    public record ActionDefinition(String action, boolean platformOnly) {
    }

    public record MenuDefinition(String menuKey, String menuName, String groupName,
                                 List<ActionDefinition> actions) {
    }

    public record PermissionDefinition(String permCode, String menuKey, String menuName, String groupName,
                                       String action, String actionName, boolean platformOnly, int sort) {
    }
}

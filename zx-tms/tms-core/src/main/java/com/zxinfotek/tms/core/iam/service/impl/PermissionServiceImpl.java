package com.zxinfotek.tms.core.iam.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zxinfotek.tms.common.enums.DataScope;
import com.zxinfotek.tms.common.enums.EnableStatus;
import com.zxinfotek.tms.common.exception.NotFoundException;
import com.zxinfotek.tms.core.iam.api.PermissionService;
import com.zxinfotek.tms.core.iam.api.model.PermissionCatalogVO;
import com.zxinfotek.tms.core.iam.constant.PermissionCatalog;
import com.zxinfotek.tms.core.iam.entity.MemberEntity;
import com.zxinfotek.tms.core.iam.entity.MemberRoleEntity;
import com.zxinfotek.tms.core.iam.entity.OrgEntity;
import com.zxinfotek.tms.core.iam.entity.PermissionEntity;
import com.zxinfotek.tms.core.iam.entity.RoleEntity;
import com.zxinfotek.tms.core.iam.entity.RolePermEntity;
import com.zxinfotek.tms.core.iam.entity.TenantEntity;
import com.zxinfotek.tms.core.iam.entity.TenantFeatureEntity;
import com.zxinfotek.tms.core.iam.mapper.MemberMapper;
import com.zxinfotek.tms.core.iam.mapper.MemberRoleMapper;
import com.zxinfotek.tms.core.iam.mapper.OrgMapper;
import com.zxinfotek.tms.core.iam.mapper.PermissionMapper;
import com.zxinfotek.tms.core.iam.mapper.RoleMapper;
import com.zxinfotek.tms.core.iam.mapper.RolePermMapper;
import com.zxinfotek.tms.core.iam.mapper.TenantFeatureMapper;
import com.zxinfotek.tms.core.iam.mapper.TenantMapper;
import com.zxinfotek.tms.infra.context.RequestContext;
import com.zxinfotek.tms.infra.context.RequestContextHolder;
import com.zxinfotek.tms.infra.mybatis.SnowflakeIdentifierGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限目录维护与有效权限计算，平台与客户走同一条计算路径（详细设计 7.1）。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    private static final Logger log = LoggerFactory.getLogger(PermissionServiceImpl.class);

    private final PermissionMapper permissionMapper;
    private final MemberMapper memberMapper;
    private final MemberRoleMapper memberRoleMapper;
    private final RoleMapper roleMapper;
    private final RolePermMapper rolePermMapper;
    private final TenantMapper tenantMapper;
    private final TenantFeatureMapper tenantFeatureMapper;
    private final OrgMapper orgMapper;
    private final SnowflakeIdentifierGenerator idGenerator;

    public PermissionServiceImpl(PermissionMapper permissionMapper,
                                 MemberMapper memberMapper,
                                 MemberRoleMapper memberRoleMapper,
                                 RoleMapper roleMapper,
                                 RolePermMapper rolePermMapper,
                                 TenantMapper tenantMapper,
                                 TenantFeatureMapper tenantFeatureMapper,
                                 OrgMapper orgMapper,
                                 SnowflakeIdentifierGenerator idGenerator) {
        this.permissionMapper = permissionMapper;
        this.memberMapper = memberMapper;
        this.memberRoleMapper = memberRoleMapper;
        this.roleMapper = roleMapper;
        this.rolePermMapper = rolePermMapper;
        this.tenantMapper = tenantMapper;
        this.tenantFeatureMapper = tenantFeatureMapper;
        this.orgMapper = orgMapper;
        this.idGenerator = idGenerator;
    }

    /**
     * 同步权限目录：按权限码 upsert，目录中已移除的权限码置为停用而非物理删除，保留历史角色配置。
     *
     * @author zxinfotek
     * @since 2026-09-18
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncCatalog() {
        List<PermissionCatalog.PermissionDefinition> definitions = PermissionCatalog.permissions();
        for (PermissionCatalog.PermissionDefinition definition : definitions) {
            permissionMapper.upsert(idGenerator.nextId(), definition.permCode(), definition.menuKey(),
                    definition.menuName(), definition.groupName(), definition.action(),
                    definition.actionName(), definition.platformOnly() ? 1 : 0, definition.sort());
        }
        Set<String> current = definitions.stream()
                .map(PermissionCatalog.PermissionDefinition::permCode)
                .collect(Collectors.toSet());
        List<PermissionEntity> stored = permissionMapper.selectList(Wrappers.emptyWrapper());
        for (PermissionEntity entity : stored) {
            if (!current.contains(entity.getPermCode()) && entity.getStatus() != EnableStatus.DISABLED) {
                entity.setStatus(EnableStatus.DISABLED);
                permissionMapper.updateById(entity);
            }
        }
        log.info("权限目录同步完成，权限码 {} 个", definitions.size());
    }

    /**
     * 计算成员有效权限：角色权限码按租户已开通菜单与权限目录平台标注过滤后，按权限码取最大数据范围。
     *
     * @author zxinfotek
     * @since 2026-09-18
     */
    @Override
    public EffectivePermissions load(Long memberId) {
        MemberEntity member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new NotFoundException("成员不存在");
        }
        TenantEntity tenant = tenantMapper.selectById(member.getTenantId());
        Integer featureVersion = tenant == null ? 0 : tenant.getFeatureVersion();
        Set<String> openedMenus = tenantMenuKeys(member.getTenantId());
        boolean platform = RequestContext.PLATFORM_TENANT_ID == member.getTenantId();

        List<Long> roleIds = memberRoleMapper
                .selectList(Wrappers.<MemberRoleEntity>lambdaQuery().eq(MemberRoleEntity::getMemberId, memberId))
                .stream().map(MemberRoleEntity::getRoleId).collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return new EffectivePermissions(Collections.emptyMap(), featureVersion,
                    member.getPermVersion(), 0);
        }
        List<RoleEntity> roles = roleMapper.selectBatchIds(roleIds);
        int rolePermVersion = roles.stream()
                .mapToInt(role -> role.getPermVersion() == null ? 0 : role.getPermVersion()).sum();

        List<Long> enabledRoleIds = roles.stream()
                .filter(role -> role.getStatus() == EnableStatus.ENABLED)
                .map(RoleEntity::getId).collect(Collectors.toList());
        Map<String, DataScope> permissions = new LinkedHashMap<>();
        if (!enabledRoleIds.isEmpty()) {
            Map<Long, DataScope> scopeByRole = roles.stream()
                    .filter(role -> enabledRoleIds.contains(role.getId()))
                    .collect(Collectors.toMap(RoleEntity::getId, RoleEntity::getDataScope));
            List<RolePermEntity> rolePerms = rolePermMapper.selectList(
                    Wrappers.<RolePermEntity>lambdaQuery().in(RolePermEntity::getRoleId, enabledRoleIds));
            for (RolePermEntity rolePerm : rolePerms) {
                String permCode = rolePerm.getPermCode();
                String menuKey = menuKeyOf(permCode);
                if (!openedMenus.contains(menuKey)) {
                    continue;
                }
                if (!platform && PermissionCatalog.isPlatformOnly(permCode)) {
                    continue;
                }
                permissions.merge(permCode, scopeByRole.get(rolePerm.getRoleId()), DataScope::max);
            }
        }
        return new EffectivePermissions(permissions, featureVersion, member.getPermVersion(), rolePermVersion);
    }

    @Override
    public boolean versionChanged(Long memberId, Integer featureVersion, Integer memberPermVersion,
                                  Integer rolePermVersion) {
        MemberEntity member = memberMapper.selectById(memberId);
        if (member == null) {
            return true;
        }
        if (!equalsVersion(member.getPermVersion(), memberPermVersion)) {
            return true;
        }
        TenantEntity tenant = tenantMapper.selectById(member.getTenantId());
        if (tenant == null || !equalsVersion(tenant.getFeatureVersion(), featureVersion)) {
            return true;
        }
        List<Long> roleIds = memberRoleMapper
                .selectList(Wrappers.<MemberRoleEntity>lambdaQuery().eq(MemberRoleEntity::getMemberId, memberId))
                .stream().map(MemberRoleEntity::getRoleId).collect(Collectors.toList());
        int current = 0;
        if (!roleIds.isEmpty()) {
            current = roleMapper.selectBatchIds(roleIds).stream()
                    .mapToInt(role -> role.getPermVersion() == null ? 0 : role.getPermVersion()).sum();
        }
        return !equalsVersion(current, rolePermVersion);
    }

    @Override
    public List<PermissionCatalogVO> catalog(Long orgId) {
        Long tenantId = tenantIdOfOrg(orgId);
        boolean platform = RequestContext.PLATFORM_TENANT_ID == tenantId;
        Set<String> openedMenus = tenantMenuKeys(tenantId);
        Set<String> operatorPermissions = RequestContextHolder.get().getPermissions().keySet();

        List<PermissionCatalogVO> catalog = new ArrayList<>();
        for (PermissionCatalog.MenuDefinition menu : PermissionCatalog.menus()) {
            if (!openedMenus.contains(menu.menuKey())) {
                continue;
            }
            List<PermissionCatalogVO.ActionVO> actions = new ArrayList<>();
            for (PermissionCatalog.ActionDefinition action : menu.actions()) {
                if (action.platformOnly() && !platform) {
                    continue;
                }
                String permCode = menu.menuKey() + ":" + action.action();
                PermissionCatalogVO.ActionVO actionVO = new PermissionCatalogVO.ActionVO();
                actionVO.setPermCode(permCode);
                actionVO.setAction(action.action());
                actionVO.setActionName(PermissionCatalog.ACTION_NAMES
                        .getOrDefault(action.action(), action.action()));
                actionVO.setGrantable(operatorPermissions.contains(permCode));
                actions.add(actionVO);
            }
            if (actions.isEmpty()) {
                continue;
            }
            PermissionCatalogVO menuVO = new PermissionCatalogVO();
            menuVO.setMenuKey(menu.menuKey());
            menuVO.setMenuName(menu.menuName());
            menuVO.setGroupName(menu.groupName());
            menuVO.setActions(actions);
            catalog.add(menuVO);
        }
        return catalog;
    }

    /**
     * 可授予权限 = 操作者自身有效权限 ∩ 目标租户已开通菜单 ∩ 权限目录允许该租户使用的权限码。
     *
     * @author zxinfotek
     * @since 2026-09-18
     */
    @Override
    public Set<String> grantablePermCodes(Long orgId) {
        Long tenantId = tenantIdOfOrg(orgId);
        boolean platform = RequestContext.PLATFORM_TENANT_ID == tenantId;
        Set<String> openedMenus = tenantMenuKeys(tenantId);
        Set<String> grantable = new HashSet<>();
        for (String permCode : RequestContextHolder.get().getPermissions().keySet()) {
            if (!openedMenus.contains(menuKeyOf(permCode))) {
                continue;
            }
            if (!platform && PermissionCatalog.isPlatformOnly(permCode)) {
                continue;
            }
            grantable.add(permCode);
        }
        return grantable;
    }

    @Override
    public Set<String> tenantMenuKeys(Long tenantId) {
        return tenantFeatureMapper
                .selectList(Wrappers.<TenantFeatureEntity>lambdaQuery()
                        .eq(TenantFeatureEntity::getTenantId, tenantId))
                .stream().map(TenantFeatureEntity::getMenuKey).collect(Collectors.toSet());
    }

    private Long tenantIdOfOrg(Long orgId) {
        if (orgId == null) {
            return RequestContextHolder.get().getTenantId();
        }
        OrgEntity org = orgMapper.selectById(orgId);
        if (org == null) {
            throw new NotFoundException("机构不存在");
        }
        return org.getTenantId();
    }

    private static String menuKeyOf(String permCode) {
        int split = permCode.indexOf(':');
        return split < 0 ? permCode : permCode.substring(0, split);
    }

    private static boolean equalsVersion(Integer one, Integer other) {
        return one != null && one.equals(other);
    }
}

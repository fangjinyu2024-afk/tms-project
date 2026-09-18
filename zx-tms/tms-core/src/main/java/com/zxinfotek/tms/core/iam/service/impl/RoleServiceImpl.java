package com.zxinfotek.tms.core.iam.service.impl;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zxinfotek.tms.common.enums.DataScope;
import com.zxinfotek.tms.common.enums.EnableStatus;
import com.zxinfotek.tms.common.exception.BizException;
import com.zxinfotek.tms.common.exception.ConflictException;
import com.zxinfotek.tms.common.exception.NotFoundException;
import com.zxinfotek.tms.common.exception.PermErrorCode;
import com.zxinfotek.tms.common.exception.PermissionException;
import com.zxinfotek.tms.common.model.PageResult;
import com.zxinfotek.tms.core.iam.IamErrorCode;
import com.zxinfotek.tms.core.iam.api.PermissionService;
import com.zxinfotek.tms.core.iam.api.RoleService;
import com.zxinfotek.tms.core.iam.api.model.RoleOptionVO;
import com.zxinfotek.tms.core.iam.api.model.RoleQuery;
import com.zxinfotek.tms.core.iam.api.model.RoleSaveRequest;
import com.zxinfotek.tms.core.iam.api.model.RoleVO;
import com.zxinfotek.tms.core.iam.api.model.StatusRequest;
import com.zxinfotek.tms.core.iam.constant.PermissionCatalog;
import com.zxinfotek.tms.core.iam.entity.MemberRoleEntity;
import com.zxinfotek.tms.core.iam.entity.OrgEntity;
import com.zxinfotek.tms.core.iam.entity.RoleEntity;
import com.zxinfotek.tms.core.iam.entity.RolePermEntity;
import com.zxinfotek.tms.core.iam.enums.BuiltinRole;
import com.zxinfotek.tms.core.iam.mapper.MemberRoleMapper;
import com.zxinfotek.tms.core.iam.mapper.OrgMapper;
import com.zxinfotek.tms.core.iam.mapper.RoleMapper;
import com.zxinfotek.tms.core.iam.mapper.RolePermMapper;
import com.zxinfotek.tms.infra.context.DataScopeAssert;
import com.zxinfotek.tms.infra.context.RequestContext;
import com.zxinfotek.tms.infra.context.RequestContextHolder;
import com.zxinfotek.tms.infra.excel.ExcelExportService;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 角色与角色权限维护，含可授予权限校验、查看依赖补齐与内置角色同步（详细设计 3.2）。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
@Service
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final RolePermMapper rolePermMapper;
    private final MemberRoleMapper memberRoleMapper;
    private final OrgMapper orgMapper;
    private final PermissionService permissionService;
    private final AdminProtection adminProtection;
    private final ExcelExportService excelExportService;

    public RoleServiceImpl(RoleMapper roleMapper,
                           RolePermMapper rolePermMapper,
                           MemberRoleMapper memberRoleMapper,
                           OrgMapper orgMapper,
                           PermissionService permissionService,
                           AdminProtection adminProtection,
                           ExcelExportService excelExportService) {
        this.roleMapper = roleMapper;
        this.rolePermMapper = rolePermMapper;
        this.memberRoleMapper = memberRoleMapper;
        this.orgMapper = orgMapper;
        this.permissionService = permissionService;
        this.adminProtection = adminProtection;
        this.excelExportService = excelExportService;
    }

    @Override
    public PageResult<RoleVO> page(RoleQuery query) {
        Page<RoleEntity> page = new Page<>(query.resolvePageNum(), query.resolvePageSize());
        Page<RoleEntity> result = (Page<RoleEntity>) roleMapper.selectRolePage(page, buildWrapper(query));
        List<RoleVO> rows = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        fillMemberCount(rows);
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), rows);
    }

    @Override
    public RoleVO detail(Long id) {
        RoleEntity role = requireRole(id);
        DataScopeAssert.within(role.getTenantId(), role.getOwnerOrgPath());
        RoleVO vo = toVO(role);
        vo.setPermCodes(permCodesOf(id));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(RoleSaveRequest request) {
        RequestContext context = RequestContextHolder.get();
        Long ownerOrgId = request.getOwnerOrgId() == null ? context.getOrgId() : request.getOwnerOrgId();
        OrgEntity owner = orgMapper.selectById(ownerOrgId);
        if (owner == null) {
            throw new NotFoundException("归属机构不存在");
        }
        DataScopeAssert.within(owner.getTenantId(), owner.getOrgPath());
        assertNameUnique(ownerOrgId, request.getName(), null);

        RoleEntity role = new RoleEntity();
        role.setTenantId(owner.getTenantId());
        role.setOwnerOrgId(owner.getId());
        role.setOwnerOrgPath(owner.getOrgPath());
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setDataScope(request.getDataScope() == null ? DataScope.ORG_AND_SUB : request.getDataScope());
        role.setPermVersion(1);
        role.setStatus(EnableStatus.ENABLED);
        role.setVersion(0);
        role.setDeleted(0);
        roleMapper.insert(role);

        Set<String> granted = resolvePermCodes(request.getPermCodes(), Set.of(), ownerOrgId);
        savePermCodes(role.getId(), granted);
        return role.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, RoleSaveRequest request) {
        RoleEntity role = requireRole(id);
        assertMaintainable(role);
        assertNameUnique(role.getOwnerOrgId(), request.getName(), id);

        Set<String> existing = new HashSet<>(permCodesOf(id));
        Set<String> granted = resolvePermCodes(request.getPermCodes(), existing, role.getOwnerOrgId());

        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setDataScope(request.getDataScope());
        role.setVersion(request.getVersion());
        if (roleMapper.updateById(role) == 0) {
            throw new ConflictException("角色已被其他人修改，请刷新后重试");
        }
        savePermCodes(id, granted);
        roleMapper.increasePermVersion(id);
        adminProtection.assertAdminRemains(role.getOwnerOrgId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long copy(Long id) {
        RoleEntity source = requireRole(id);
        DataScopeAssert.within(source.getTenantId(), source.getOwnerOrgPath());
        RequestContext context = RequestContextHolder.get();
        Long ownerOrgId = context.getOrgId();
        OrgEntity owner = orgMapper.selectById(ownerOrgId);
        if (owner == null) {
            throw new NotFoundException("归属机构不存在");
        }

        String name = nextCopyName(ownerOrgId, source.getName());
        RoleEntity role = new RoleEntity();
        role.setTenantId(owner.getTenantId());
        role.setOwnerOrgId(owner.getId());
        role.setOwnerOrgPath(owner.getOrgPath());
        role.setName(name);
        role.setDescription(source.getDescription());
        role.setDataScope(source.getDataScope());
        role.setPermVersion(1);
        role.setStatus(EnableStatus.ENABLED);
        role.setVersion(0);
        role.setDeleted(0);
        roleMapper.insert(role);

        // 复制角色只带入操作者当前可授予的权限项
        Set<String> grantable = permissionService.grantablePermCodes(ownerOrgId);
        Set<String> copied = permCodesOf(id).stream().filter(grantable::contains)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        savePermCodes(role.getId(), withViewDependency(copied));
        return role.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, StatusRequest request) {
        RoleEntity role = requireRole(id);
        assertMaintainable(role);
        role.setStatus(request.getStatus());
        roleMapper.updateById(role);
        roleMapper.increasePermVersion(id);
        if (request.getStatus() == EnableStatus.DISABLED) {
            adminProtection.assertAdminRemains(role.getOwnerOrgId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        RoleEntity role = requireRole(id);
        assertMaintainable(role);
        Long referenced = memberRoleMapper.selectCount(
                Wrappers.<MemberRoleEntity>lambdaQuery().eq(MemberRoleEntity::getRoleId, id));
        if (referenced != null && referenced > 0) {
            throw new BizException(IamErrorCode.ROLE_003,
                    "角色已被 " + referenced + " 名成员引用，不能删除");
        }
        rolePermMapper.delete(Wrappers.<RolePermEntity>lambdaQuery().eq(RolePermEntity::getRoleId, id));
        roleMapper.deleteById(id);
        adminProtection.assertAdminRemains(role.getOwnerOrgId());
    }

    @Override
    public List<RoleOptionVO> assignableOptions(Long orgId) {
        OrgEntity org = orgMapper.selectById(orgId);
        if (org == null) {
            throw new NotFoundException("机构不存在");
        }
        Set<String> grantable = permissionService.grantablePermCodes(orgId);
        Set<String> openedMenus = permissionService.tenantMenuKeys(org.getTenantId());
        List<RoleEntity> roles = roleMapper.selectAssignableRoles(org.getTenantId(), org.getOrgPath());
        Map<Long, List<String>> permsByRole = permCodesOf(roles.stream()
                .map(RoleEntity::getId).collect(Collectors.toList()));
        List<RoleOptionVO> options = new ArrayList<>();
        for (RoleEntity role : roles) {
            if (role.getStatus() != EnableStatus.ENABLED) {
                continue;
            }
            RoleOptionVO option = new RoleOptionVO();
            option.setId(role.getId());
            option.setName(role.getName());
            option.setDataScope(role.getDataScope() == null ? null : role.getDataScope().getCode());
            option.setBuiltin(role.getBuiltinCode() != null);
            option.setOwnerOrgName(orgName(role.getOwnerOrgId()));
            List<String> effective = permsByRole.getOrDefault(role.getId(), List.of()).stream()
                    .filter(code -> openedMenus.contains(code.substring(0, code.indexOf(':'))))
                    .collect(Collectors.toList());
            option.setAssignable(grantable.containsAll(effective));
            options.add(option);
        }
        return options;
    }

    @Override
    public String export(RoleQuery query) {
        List<RoleEntity> roles = roleMapper.selectRolePage(
                new Page<>(1, 10000), buildWrapper(query)).getRecords();
        List<RoleExportRow> rows = roles.stream().map(role -> {
            RoleExportRow row = new RoleExportRow();
            row.setName(role.getName());
            row.setOwnerOrgName(orgName(role.getOwnerOrgId()));
            row.setDataScope(role.getDataScope() == null ? "" : role.getDataScope().getLabel());
            row.setBuiltin(role.getBuiltinCode() != null ? "是" : "否");
            row.setStatus(role.getStatus() == null ? "" : role.getStatus().getLabel());
            row.setDescription(role.getDescription());
            row.setCreateTime(role.getCreateTime());
            return row;
        }).collect(Collectors.toList());
        return excelExportService.export("role", RequestContextHolder.get().getTenantId(),
                "角色", RoleExportRow.class, rows);
    }

    /**
     * 内置角色同步：不存在则创建，存在则按权限目录补齐权限码，保证新增功能自动纳入内置管理员角色。
     *
     * @author zxinfotek
     * @since 2026-09-18
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long ensureBuiltinRole(Long tenantId, Long ownerOrgId, String ownerOrgPath, BuiltinRole builtinRole) {
        RoleEntity role = roleMapper.selectOne(Wrappers.<RoleEntity>lambdaQuery()
                .eq(RoleEntity::getTenantId, tenantId)
                .eq(RoleEntity::getBuiltinCode, builtinRole.name())
                .last("LIMIT 1"));
        if (role == null) {
            role = new RoleEntity();
            role.setTenantId(tenantId);
            role.setOwnerOrgId(ownerOrgId);
            role.setOwnerOrgPath(ownerOrgPath);
            role.setName(builtinRole.getRoleName());
            role.setDescription(builtinRole.getDescription());
            role.setDataScope(DataScope.ORG_AND_SUB);
            role.setBuiltinCode(builtinRole.name());
            role.setPermVersion(1);
            role.setStatus(EnableStatus.ENABLED);
            role.setVersion(0);
            role.setDeleted(0);
            roleMapper.insert(role);
        }
        Set<String> expected = new LinkedHashSet<>(builtinRole.permCodes());
        Set<String> current = new HashSet<>(permCodesOf(role.getId()));
        if (!current.equals(expected)) {
            savePermCodes(role.getId(), expected);
            roleMapper.increasePermVersion(role.getId());
        }
        return role.getId();
    }

    @Override
    public void assertAssignable(List<Long> roleIds, Long tenantId, String targetOrgPath) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new BizException(IamErrorCode.MEMBER_004);
        }
        List<RoleEntity> roles = roleMapper.selectBatchIds(roleIds);
        if (roles.size() != new HashSet<>(roleIds).size()) {
            throw new NotFoundException("存在不可用的角色");
        }
        Set<String> grantable = permissionService.grantablePermCodes(orgIdOfPath(targetOrgPath));
        Set<String> openedMenus = permissionService.tenantMenuKeys(tenantId);
        Map<Long, List<String>> permsByRole = permCodesOf(roleIds);
        for (RoleEntity role : roles) {
            if (!role.getTenantId().equals(tenantId)
                    || !targetOrgPath.startsWith(role.getOwnerOrgPath())) {
                throw new PermissionException(PermErrorCode.PERM_002, "角色「" + role.getName() + "」不在可分配范围内");
            }
            if (role.getStatus() != EnableStatus.ENABLED) {
                throw new BizException(IamErrorCode.ROLE_001, "角色「" + role.getName() + "」已停用");
            }
            // 未开通菜单的权限码不会进入有效权限，比对时按租户已开通范围裁剪后再判断是否越权
            List<String> effective = permsByRole.getOrDefault(role.getId(), List.of()).stream()
                    .filter(code -> openedMenus.contains(code.substring(0, code.indexOf(':'))))
                    .collect(Collectors.toList());
            if (!grantable.containsAll(effective)) {
                throw new PermissionException(PermErrorCode.PERM_003,
                        "无权分配角色「" + role.getName() + "」");
            }
        }
    }

    /**
     * 计算最终保存的权限码：越权项整体拒绝，保留操作者无权维护的既有权限，并补齐查看依赖。
     *
     * @author zxinfotek
     * @since 2026-09-18
     */
    private Set<String> resolvePermCodes(List<String> submitted, Set<String> existing, Long ownerOrgId) {
        Set<String> grantable = permissionService.grantablePermCodes(ownerOrgId);
        Set<String> requested = submitted == null ? Set.of() : new LinkedHashSet<>(submitted);
        List<String> unknown = requested.stream()
                .filter(code -> !PermissionCatalog.exists(code)).collect(Collectors.toList());
        if (!unknown.isEmpty()) {
            throw new BizException(PermErrorCode.PERM_003, "存在未登记的权限码", unknown);
        }
        List<String> beyond = requested.stream()
                .filter(code -> !grantable.contains(code) && !existing.contains(code))
                .collect(Collectors.toList());
        if (!beyond.isEmpty()) {
            throw new BizException(PermErrorCode.PERM_003, "包含无权授予的权限项", beyond);
        }
        Set<String> result = new LinkedHashSet<>(requested);
        // 操作者无权维护的既有权限保留只读，不因本次保存被移除
        existing.stream().filter(code -> !grantable.contains(code)).forEach(result::add);
        return withViewDependency(result);
    }

    /** 勾选任一操作时自动补齐该菜单的查看权限。 */
    private Set<String> withViewDependency(Set<String> permCodes) {
        Set<String> result = new LinkedHashSet<>(permCodes);
        Set<String> menus = permCodes.stream()
                .map(code -> code.substring(0, code.indexOf(':')))
                .collect(Collectors.toSet());
        for (String menuKey : menus) {
            String viewCode = menuKey + ":view";
            if (PermissionCatalog.exists(viewCode)) {
                result.add(viewCode);
            }
        }
        return result;
    }

    private void savePermCodes(Long roleId, Set<String> permCodes) {
        rolePermMapper.delete(Wrappers.<RolePermEntity>lambdaQuery().eq(RolePermEntity::getRoleId, roleId));
        for (String permCode : permCodes) {
            RolePermEntity entity = new RolePermEntity();
            entity.setRoleId(roleId);
            entity.setPermCode(permCode);
            rolePermMapper.insert(entity);
        }
    }

    private List<String> permCodesOf(Long roleId) {
        return rolePermMapper.selectList(Wrappers.<RolePermEntity>lambdaQuery()
                        .eq(RolePermEntity::getRoleId, roleId)).stream()
                .map(RolePermEntity::getPermCode).collect(Collectors.toList());
    }

    private Map<Long, List<String>> permCodesOf(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Map.of();
        }
        return rolePermMapper.selectList(Wrappers.<RolePermEntity>lambdaQuery()
                        .in(RolePermEntity::getRoleId, roleIds)).stream()
                .collect(Collectors.groupingBy(RolePermEntity::getRoleId,
                        Collectors.mapping(RolePermEntity::getPermCode, Collectors.toList())));
    }

    private void assertMaintainable(RoleEntity role) {
        if (role.getBuiltinCode() != null) {
            throw new BizException(IamErrorCode.ROLE_002);
        }
        DataScopeAssert.within(role.getTenantId(), role.getOwnerOrgPath());
        // 归属机构是唯一有权维护该角色的机构
        if (!role.getOwnerOrgId().equals(RequestContextHolder.get().getOrgId())) {
            throw new PermissionException(PermErrorCode.PERM_002, "只有角色归属机构可以维护该角色");
        }
    }

    private void assertNameUnique(Long ownerOrgId, String name, Long excludeId) {
        LambdaQueryWrapper<RoleEntity> wrapper = Wrappers.<RoleEntity>lambdaQuery()
                .eq(RoleEntity::getOwnerOrgId, ownerOrgId)
                .eq(RoleEntity::getName, name);
        if (excludeId != null) {
            wrapper.ne(RoleEntity::getId, excludeId);
        }
        if (roleMapper.selectCount(wrapper) > 0) {
            throw new BizException(IamErrorCode.ROLE_001);
        }
    }

    private String nextCopyName(Long ownerOrgId, String sourceName) {
        String base = sourceName.length() > 44 ? sourceName.substring(0, 44) : sourceName;
        for (int index = 1; index < 100; index++) {
            String candidate = base + " 副本" + (index == 1 ? "" : index);
            Long count = roleMapper.selectCount(Wrappers.<RoleEntity>lambdaQuery()
                    .eq(RoleEntity::getOwnerOrgId, ownerOrgId)
                    .eq(RoleEntity::getName, candidate));
            if (count == null || count == 0) {
                return candidate;
            }
        }
        throw new BizException(IamErrorCode.ROLE_001);
    }

    private LambdaQueryWrapper<RoleEntity> buildWrapper(RoleQuery query) {
        LambdaQueryWrapper<RoleEntity> wrapper = Wrappers.<RoleEntity>lambdaQuery()
                .eq(RoleEntity::getDeleted, 0);
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            wrapper.like(RoleEntity::getName, query.getKeyword());
        }
        if (query.getStatus() != null) {
            wrapper.eq(RoleEntity::getStatus, query.getStatus());
        }
        if (query.getOrgId() != null) {
            OrgEntity org = orgMapper.selectById(query.getOrgId());
            if (org != null) {
                wrapper.likeRight(RoleEntity::getOwnerOrgPath, org.getOrgPath());
            }
        }
        wrapper.orderByDesc(RoleEntity::getCreateTime);
        return wrapper;
    }

    private void fillMemberCount(List<RoleVO> rows) {
        if (rows.isEmpty()) {
            return;
        }
        List<Long> roleIds = rows.stream().map(RoleVO::getId).collect(Collectors.toList());
        Map<Long, Long> counts = memberRoleMapper.selectList(Wrappers.<MemberRoleEntity>lambdaQuery()
                        .in(MemberRoleEntity::getRoleId, roleIds)).stream()
                .collect(Collectors.groupingBy(MemberRoleEntity::getRoleId, Collectors.counting()));
        rows.forEach(row -> row.setMemberCount(counts.getOrDefault(row.getId(), 0L).intValue()));
    }

    private RoleVO toVO(RoleEntity role) {
        RoleVO vo = new RoleVO();
        vo.setId(role.getId());
        vo.setTenantId(role.getTenantId());
        vo.setName(role.getName());
        vo.setDescription(role.getDescription());
        vo.setOwnerOrgId(role.getOwnerOrgId());
        vo.setOwnerOrgName(orgName(role.getOwnerOrgId()));
        vo.setDataScope(role.getDataScope() == null ? null : role.getDataScope().getCode());
        vo.setDataScopeLabel(role.getDataScope() == null ? null : role.getDataScope().getLabel());
        vo.setBuiltin(role.getBuiltinCode() != null);
        vo.setStatus(role.getStatus() == null ? null : role.getStatus().getCode());
        vo.setVersion(role.getVersion());
        vo.setCreateTime(role.getCreateTime());
        return vo;
    }

    private String orgName(Long orgId) {
        OrgEntity org = orgMapper.selectById(orgId);
        return org == null ? null : org.getName();
    }

    private Long orgIdOfPath(String orgPath) {
        String trimmed = orgPath.substring(0, orgPath.length() - 1);
        return Long.valueOf(trimmed.substring(trimmed.lastIndexOf('/') + 1));
    }

    private RoleEntity requireRole(Long id) {
        RoleEntity role = roleMapper.selectById(id);
        if (role == null) {
            throw new NotFoundException("角色不存在");
        }
        return role;
    }

    @Data
    public static class RoleExportRow {
        @ExcelProperty("角色名称")
        private String name;
        @ExcelProperty("归属机构")
        private String ownerOrgName;
        @ExcelProperty("可管理范围")
        private String dataScope;
        @ExcelProperty("内置")
        private String builtin;
        @ExcelProperty("状态")
        private String status;
        @ExcelProperty("说明")
        private String description;
        @ExcelProperty("创建时间")
        private LocalDateTime createTime;
    }
}

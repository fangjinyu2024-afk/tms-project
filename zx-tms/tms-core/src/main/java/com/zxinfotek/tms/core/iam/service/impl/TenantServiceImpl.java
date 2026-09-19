package com.zxinfotek.tms.core.iam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zxinfotek.tms.common.enums.EnableStatus;
import com.zxinfotek.tms.common.enums.OrgType;
import com.zxinfotek.tms.common.enums.SessionInvalidReason;
import com.zxinfotek.tms.common.exception.BizException;
import com.zxinfotek.tms.common.exception.NotFoundException;
import com.zxinfotek.tms.common.exception.PermErrorCode;
import com.zxinfotek.tms.common.model.PageResult;
import com.zxinfotek.tms.common.util.UtcTimes;
import com.zxinfotek.tms.core.iam.IamErrorCode;
import com.zxinfotek.tms.core.iam.api.LoginSessionService;
import com.zxinfotek.tms.core.iam.api.MemberService;
import com.zxinfotek.tms.core.iam.api.RoleService;
import com.zxinfotek.tms.core.iam.api.TenantReferenceProvider;
import com.zxinfotek.tms.core.iam.api.TenantService;
import com.zxinfotek.tms.core.iam.api.model.MemberCreateResultVO;
import com.zxinfotek.tms.core.iam.api.model.MemberSaveRequest;
import com.zxinfotek.tms.core.iam.api.model.StatusRequest;
import com.zxinfotek.tms.core.iam.api.model.TenantCreateResultVO;
import com.zxinfotek.tms.core.iam.api.model.TenantFeatureSaveRequest;
import com.zxinfotek.tms.core.iam.api.model.TenantFeatureVO;
import com.zxinfotek.tms.core.iam.api.model.TenantQuery;
import com.zxinfotek.tms.core.iam.api.model.TenantSaveRequest;
import com.zxinfotek.tms.core.iam.api.model.TenantVO;
import com.zxinfotek.tms.core.iam.constant.IamConstants;
import com.zxinfotek.tms.core.iam.constant.MenuKeys;
import com.zxinfotek.tms.core.iam.constant.PermissionCatalog;
import com.zxinfotek.tms.core.iam.entity.MemberEntity;
import com.zxinfotek.tms.core.iam.entity.OrgEntity;
import com.zxinfotek.tms.core.iam.entity.TenantEntity;
import com.zxinfotek.tms.core.iam.entity.TenantFeatureEntity;
import com.zxinfotek.tms.core.iam.enums.BuiltinRole;
import com.zxinfotek.tms.core.iam.mapper.MemberMapper;
import com.zxinfotek.tms.core.iam.mapper.OrgMapper;
import com.zxinfotek.tms.core.iam.mapper.TenantFeatureMapper;
import com.zxinfotek.tms.core.iam.mapper.TenantMapper;
import com.zxinfotek.tms.core.product.api.ProductModelService;
import com.zxinfotek.tms.core.product.api.model.ModelOptionVO;
import com.zxinfotek.tms.infra.context.RequestContextHolder;
import com.zxinfotek.tms.infra.excel.ExcelExportService;
import com.zxinfotek.tms.infra.i18n.I18nMessages;
import com.zxinfotek.tms.infra.mybatis.SnowflakeIdentifierGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 客户档案与功能授权：客户、根机构、管理员与功能授权在同一事务内创建（详细设计 3.3）。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
@Service
public class TenantServiceImpl implements TenantService {

    private final TenantMapper tenantMapper;
    private final TenantFeatureMapper tenantFeatureMapper;
    private final OrgMapper orgMapper;
    private final MemberMapper memberMapper;
    private final MemberService memberService;
    private final RoleService roleService;
    private final LoginSessionService loginSessionService;
    private final ProductModelService productModelService;
    private final SnowflakeIdentifierGenerator idGenerator;
    private final ExcelExportService excelExportService;
    private final List<TenantReferenceProvider> referenceProviders;

    public TenantServiceImpl(TenantMapper tenantMapper,
                             TenantFeatureMapper tenantFeatureMapper,
                             OrgMapper orgMapper,
                             MemberMapper memberMapper,
                             MemberService memberService,
                             RoleService roleService,
                             LoginSessionService loginSessionService,
                             ProductModelService productModelService,
                             SnowflakeIdentifierGenerator idGenerator,
                             ExcelExportService excelExportService,
                             List<TenantReferenceProvider> referenceProviders) {
        this.tenantMapper = tenantMapper;
        this.tenantFeatureMapper = tenantFeatureMapper;
        this.orgMapper = orgMapper;
        this.memberMapper = memberMapper;
        this.memberService = memberService;
        this.roleService = roleService;
        this.loginSessionService = loginSessionService;
        this.productModelService = productModelService;
        this.idGenerator = idGenerator;
        this.excelExportService = excelExportService;
        this.referenceProviders = referenceProviders;
    }

    @Override
    public PageResult<TenantVO> page(TenantQuery query) {
        Page<TenantEntity> page = new Page<>(query.resolvePageNum(), query.resolvePageSize());
        Page<TenantEntity> result = tenantMapper.selectPage(page, buildWrapper(query));
        List<TenantVO> rows = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        rows.forEach(this::fillModels);
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), rows);
    }

    @Override
    public TenantVO detail(Long id) {
        TenantEntity tenant = requireTenant(id);
        TenantVO vo = toVO(tenant);
        fillModels(vo);
        vo.setMenuKeys(menuKeysOf(id));
        return vo;
    }

    /**
     * 新增客户：客户记录、根机构、内置角色与功能授权、客户管理员四步在同一事务内完成。
     *
     * @author zxinfotek
     * @since 2026-09-18
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantCreateResultVO create(TenantSaveRequest request) {
        assertNameUnique(request.getName(), null);

        long tenantId = idGenerator.nextId();
        long rootOrgId = idGenerator.nextId();

        OrgEntity rootOrg = new OrgEntity();
        rootOrg.setId(rootOrgId);
        rootOrg.setTenantId(tenantId);
        rootOrg.setParentId(IamConstants.PLATFORM_ROOT_ORG_ID);
        rootOrg.setOrgPath(IamConstants.PLATFORM_ORG_PATH + rootOrgId + "/");
        rootOrg.setOrgType(OrgType.TENANT_ROOT);
        rootOrg.setName(request.getName());
        rootOrg.setContactName(request.getContactName());
        rootOrg.setContactPhone(request.getContactPhone());
        rootOrg.setStatus(EnableStatus.ENABLED);
        rootOrg.setDeleted(0);
        orgMapper.insert(rootOrg);

        TenantEntity tenant = new TenantEntity();
        tenant.setId(tenantId);
        tenant.setName(request.getName());
        tenant.setRootOrgId(rootOrgId);
        tenant.setContactName(request.getContactName());
        tenant.setContactPhone(request.getContactPhone());
        tenant.setCountry(request.getCountry());
        tenant.setProvince(request.getProvince());
        tenant.setCity(request.getCity());
        tenant.setRemark(request.getRemark());
        tenant.setStatus(EnableStatus.ENABLED);
        tenant.setAuthCodeChannel(request.getAuthCodeChannel() == null ? "TOOL" : request.getAuthCodeChannel());
        tenant.setFeatureVersion(1);
        tenant.setDeleted(0);
        tenantMapper.insert(tenant);

        saveFeatureRows(tenantId, normalizeMenuKeys(request.getMenuKeys()));
        productModelService.saveTenantModels(tenantId, request.getModelIds());

        Long adminRoleId = roleService.ensureBuiltinRole(tenantId, rootOrgId, rootOrg.getOrgPath(),
                BuiltinRole.TENANT_ADMIN);
        roleService.ensureBuiltinRole(tenantId, rootOrgId, rootOrg.getOrgPath(), BuiltinRole.BRANCH_ADMIN);

        TenantCreateResultVO result = new TenantCreateResultVO();
        result.setTenantId(tenantId);
        result.setRootOrgId(rootOrgId);
        if (request.getAdminAccount() != null && !request.getAdminAccount().isBlank()) {
            MemberSaveRequest adminRequest = new MemberSaveRequest();
            adminRequest.setOrgId(rootOrgId);
            adminRequest.setAccount(request.getAdminAccount());
            adminRequest.setNickname(request.getAdminNickname() == null || request.getAdminNickname().isBlank()
                    ? I18nMessages.get("msg.org.defaultAdminNickname", request.getName())
                    : request.getAdminNickname());
            adminRequest.setRoleIds(List.of(adminRoleId));
            MemberCreateResultVO admin = memberService.create(adminRequest);
            result.setAdminMemberId(admin.getMemberId());
            result.setAdminAccount(admin.getAccount());
            result.setInitialPassword(admin.getInitialPassword());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, TenantSaveRequest request) {
        TenantEntity tenant = requireTenant(id);
        assertNotPlatform(id);
        assertNameUnique(request.getName(), id);

        tenant.setName(request.getName());
        tenant.setContactName(request.getContactName());
        tenant.setContactPhone(request.getContactPhone());
        tenant.setCountry(request.getCountry());
        tenant.setProvince(request.getProvince());
        tenant.setCity(request.getCity());
        tenant.setRemark(request.getRemark());
        if (request.getAuthCodeChannel() != null) {
            tenant.setAuthCodeChannel(request.getAuthCodeChannel());
        }
        tenantMapper.updateById(tenant);

        OrgEntity rootOrg = orgMapper.selectById(tenant.getRootOrgId());
        if (rootOrg != null && !rootOrg.getName().equals(request.getName())) {
            rootOrg.setName(request.getName());
            orgMapper.updateById(rootOrg);
        }
        productModelService.saveTenantModels(id, request.getModelIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, StatusRequest request) {
        TenantEntity tenant = requireTenant(id);
        assertNotPlatform(id);
        tenant.setStatus(request.getStatus());
        tenantMapper.updateById(tenant);
        OrgEntity rootOrg = orgMapper.selectById(tenant.getRootOrgId());
        if (rootOrg != null) {
            rootOrg.setStatus(request.getStatus());
            orgMapper.updateById(rootOrg);
        }
        if (request.getStatus() == EnableStatus.DISABLED) {
            loginSessionService.invalidateByTenant(id, SessionInvalidReason.ORG_DISABLED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        TenantEntity tenant = requireTenant(id);
        assertNotPlatform(id);
        List<String> blocking = new ArrayList<>();
        Long orgCount = orgMapper.selectCount(Wrappers.<OrgEntity>lambdaQuery()
                .eq(OrgEntity::getTenantId, id)
                .eq(OrgEntity::getOrgType, OrgType.BRANCH)
                .eq(OrgEntity::getDeleted, 0));
        if (orgCount != null && orgCount > 0) {
            blocking.add(I18nMessages.get("msg.block.subOrg", orgCount));
        }
        Long memberCount = memberMapper.selectCount(Wrappers.<MemberEntity>lambdaQuery()
                .eq(MemberEntity::getTenantId, id));
        if (memberCount != null && memberCount > 0) {
            blocking.add(I18nMessages.get("msg.block.member", memberCount));
        }
        referenceProviders.forEach(provider -> blocking.addAll(provider.blockingReferences(id)));
        if (!blocking.isEmpty()) {
            throw new BizException(IamErrorCode.TENANT_003, "msg.tenant.deleteBlocked",
                    String.join(I18nMessages.get("msg.common.separator"), blocking));
        }
        tenantMapper.deleteById(id);
        orgMapper.deleteById(tenant.getRootOrgId());
        tenantFeatureMapper.delete(Wrappers.<TenantFeatureEntity>lambdaQuery()
                .eq(TenantFeatureEntity::getTenantId, id));
        productModelService.removeTenantModels(id);
    }

    @Override
    public TenantFeatureVO features(Long id) {
        requireTenant(id);
        TenantFeatureVO vo = new TenantFeatureVO();
        vo.setTenantId(id);
        vo.setMenuKeys(menuKeysOf(id));
        vo.setOptions(featureOptions());
        return vo;
    }

    /**
     * 保存客户功能授权：校验菜单存在且对客户可用，工作台强制包含，保存后自增功能版本。
     *
     * @author zxinfotek
     * @since 2026-09-18
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantFeatureVO saveFeatures(Long id, TenantFeatureSaveRequest request) {
        requireTenant(id);
        assertNotPlatform(id);
        Set<String> target = normalizeMenuKeys(request.getMenuKeys());
        Set<String> current = new LinkedHashSet<>(menuKeysOf(id));

        List<String> added = target.stream().filter(menu -> !current.contains(menu))
                .collect(Collectors.toList());
        List<String> removed = current.stream().filter(menu -> !target.contains(menu))
                .collect(Collectors.toList());
        if (!removed.isEmpty()) {
            tenantFeatureMapper.delete(Wrappers.<TenantFeatureEntity>lambdaQuery()
                    .eq(TenantFeatureEntity::getTenantId, id)
                    .in(TenantFeatureEntity::getMenuKey, removed));
        }
        for (String menuKey : added) {
            insertFeature(id, menuKey);
        }
        tenantMapper.increaseFeatureVersion(id);

        TenantFeatureVO vo = new TenantFeatureVO();
        vo.setTenantId(id);
        vo.setMenuKeys(new ArrayList<>(target));
        vo.setAdded(added);
        vo.setRemoved(removed);
        vo.setOptions(featureOptions());
        return vo;
    }

    @Override
    public String export(TenantQuery query) {
        List<TenantEntity> tenants = tenantMapper.selectPage(new Page<>(1, 10000), buildWrapper(query))
                .getRecords();
        List<List<Object>> rows = tenants.stream().map(tenant -> List.<Object>of(
                nullToEmpty(tenant.getName()),
                nullToEmpty(tenant.getContactName()),
                nullToEmpty(tenant.getContactPhone()),
                String.join("/", java.util.stream.Stream
                        .of(tenant.getCountry(), tenant.getProvince(), tenant.getCity())
                        .filter(value -> value != null && !value.isBlank()).toList()),
                nullToEmpty(I18nMessages.label(tenant.getStatus())),
                UtcTimes.formatCompact(tenant.getCreateTime()))).collect(Collectors.toList());
        return excelExportService.export("tenant", RequestContextHolder.get().getTenantId(),
                "export.sheet.tenant",
                List.of("export.column.tenantName", "export.column.contactName", "export.column.contactPhone",
                        "export.column.region", "export.column.status", "export.column.createTime"),
                rows);
    }

    /**
     * 平台租户初始化：平台与客户共用同一套租户模型，平台功能授权初始化为全部菜单。
     *
     * @author zxinfotek
     * @since 2026-09-18
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ensurePlatformTenant() {
        OrgEntity rootOrg = orgMapper.selectById(IamConstants.PLATFORM_ROOT_ORG_ID);
        if (rootOrg == null) {
            rootOrg = new OrgEntity();
            rootOrg.setId(IamConstants.PLATFORM_ROOT_ORG_ID);
            rootOrg.setTenantId(IamConstants.PLATFORM_TENANT_ID);
            rootOrg.setParentId(0L);
            rootOrg.setOrgPath(IamConstants.PLATFORM_ORG_PATH);
            rootOrg.setOrgType(OrgType.PLATFORM);
            rootOrg.setName(IamConstants.PLATFORM_NAME);
            rootOrg.setStatus(EnableStatus.ENABLED);
            rootOrg.setDeleted(0);
            orgMapper.insert(rootOrg);
        }
        TenantEntity tenant = tenantMapper.selectById(IamConstants.PLATFORM_TENANT_ID);
        if (tenant == null) {
            tenant = new TenantEntity();
            tenant.setId(IamConstants.PLATFORM_TENANT_ID);
            tenant.setName(IamConstants.PLATFORM_NAME);
            tenant.setRootOrgId(IamConstants.PLATFORM_ROOT_ORG_ID);
            tenant.setStatus(EnableStatus.ENABLED);
            tenant.setAuthCodeChannel("TOOL");
            tenant.setFeatureVersion(1);
            tenant.setDeleted(0);
            tenantMapper.insert(tenant);
        }
        Set<String> opened = new LinkedHashSet<>(menuKeysOf(IamConstants.PLATFORM_TENANT_ID));
        List<String> missing = PermissionCatalog.allMenuKeys().stream()
                .filter(menuKey -> !opened.contains(menuKey)).collect(Collectors.toList());
        for (String menuKey : missing) {
            insertFeature(IamConstants.PLATFORM_TENANT_ID, menuKey);
        }
        if (!missing.isEmpty()) {
            tenantMapper.increaseFeatureVersion(IamConstants.PLATFORM_TENANT_ID);
        }
    }

    @Override
    public String tenantName(Long tenantId) {
        TenantEntity tenant = tenantMapper.selectById(tenantId);
        return tenant == null ? null : tenant.getName();
    }

    private void saveFeatureRows(Long tenantId, Set<String> menuKeys) {
        for (String menuKey : menuKeys) {
            insertFeature(tenantId, menuKey);
        }
    }

    private void insertFeature(Long tenantId, String menuKey) {
        TenantFeatureEntity entity = new TenantFeatureEntity();
        entity.setTenantId(tenantId);
        entity.setMenuKey(menuKey);
        entity.setCreateTime(UtcTimes.now());
        tenantFeatureMapper.insert(entity);
    }

    /** 客户可选菜单校验：必须是权限目录中对客户开放的菜单，工作台强制包含且不可取消。 */
    private Set<String> normalizeMenuKeys(List<String> menuKeys) {
        List<String> selectable = PermissionCatalog.tenantSelectableMenuKeys();
        Set<String> result = new LinkedHashSet<>();
        result.add(MenuKeys.HOME);
        if (menuKeys != null) {
            for (String menuKey : menuKeys) {
                if (!selectable.contains(menuKey)) {
                    throw new BizException(PermErrorCode.PERM_004, "msg.tenant.menuNotSelectable", menuKey);
                }
                result.add(menuKey);
            }
        }
        return result;
    }

    @Override
    public List<TenantFeatureVO.MenuOptionVO> featureOptions() {
        List<String> selectable = PermissionCatalog.tenantSelectableMenuKeys();
        return PermissionCatalog.menus().stream()
                .filter(menu -> selectable.contains(menu.menuKey()))
                .map(menu -> {
                    TenantFeatureVO.MenuOptionVO option = new TenantFeatureVO.MenuOptionVO();
                    option.setMenuKey(menu.menuKey());
                    option.setMenuName(I18nMessages.getOrDefault("permission.menu." + menu.menuKey(),
                            menu.menuName()));
                    option.setGroupName(I18nMessages.getOrDefault(
                            "permission.group." + menu.group().groupKey(), menu.group().name()));
                    option.setRequired(MenuKeys.HOME.equals(menu.menuKey()));
                    return option;
                }).collect(Collectors.toList());
    }

    private List<String> menuKeysOf(Long tenantId) {
        return tenantFeatureMapper.selectList(Wrappers.<TenantFeatureEntity>lambdaQuery()
                        .eq(TenantFeatureEntity::getTenantId, tenantId)).stream()
                .map(TenantFeatureEntity::getMenuKey).collect(Collectors.toList());
    }

    private LambdaQueryWrapper<TenantEntity> buildWrapper(TenantQuery query) {
        LambdaQueryWrapper<TenantEntity> wrapper = Wrappers.<TenantEntity>lambdaQuery()
                .ne(TenantEntity::getId, IamConstants.PLATFORM_TENANT_ID);
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            wrapper.like(TenantEntity::getName, query.getKeyword().trim());
        }
        if (query.getStatus() != null) {
            wrapper.eq(TenantEntity::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(TenantEntity::getCreateTime);
        return wrapper;
    }

    private void fillModels(TenantVO vo) {
        List<Long> modelIds = productModelService.tenantModelIds(vo.getId());
        vo.setModelIds(modelIds);
        vo.setModelNames(productModelService.listByIds(modelIds).stream()
                .map(ModelOptionVO::getModel).collect(Collectors.toList()));
    }

    private TenantVO toVO(TenantEntity tenant) {
        TenantVO vo = new TenantVO();
        vo.setId(tenant.getId());
        vo.setName(tenant.getName());
        vo.setRootOrgId(tenant.getRootOrgId());
        vo.setContactName(tenant.getContactName());
        vo.setContactPhone(tenant.getContactPhone());
        vo.setCountry(tenant.getCountry());
        vo.setProvince(tenant.getProvince());
        vo.setCity(tenant.getCity());
        vo.setRemark(tenant.getRemark());
        vo.setStatus(tenant.getStatus() == null ? null : tenant.getStatus().getCode());
        vo.setAuthCodeChannel(tenant.getAuthCodeChannel());
        vo.setFeatureVersion(tenant.getFeatureVersion());
        vo.setCreateTime(tenant.getCreateTime());
        return vo;
    }

    private void assertNameUnique(String name, Long excludeId) {
        LambdaQueryWrapper<TenantEntity> wrapper = Wrappers.<TenantEntity>lambdaQuery()
                .eq(TenantEntity::getName, name);
        if (excludeId != null) {
            wrapper.ne(TenantEntity::getId, excludeId);
        }
        Long exists = tenantMapper.selectCount(wrapper);
        if (exists != null && exists > 0) {
            throw new BizException(IamErrorCode.TENANT_001);
        }
    }

    private void assertNotPlatform(Long id) {
        if (id != null && id == IamConstants.PLATFORM_TENANT_ID) {
            throw new BizException(IamErrorCode.TENANT_003, "msg.tenant.platformNotAllowed");
        }
    }

    private TenantEntity requireTenant(Long id) {
        TenantEntity tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            throw new NotFoundException("msg.tenant.notFound");
        }
        return tenant;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}

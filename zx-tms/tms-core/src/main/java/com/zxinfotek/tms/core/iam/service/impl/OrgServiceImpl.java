package com.zxinfotek.tms.core.iam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zxinfotek.tms.common.enums.EnableStatus;
import com.zxinfotek.tms.common.enums.OrgType;
import com.zxinfotek.tms.common.enums.SessionInvalidReason;
import com.zxinfotek.tms.common.exception.BizException;
import com.zxinfotek.tms.common.exception.NotFoundException;
import com.zxinfotek.tms.common.model.PageResult;
import com.zxinfotek.tms.common.util.UtcTimes;
import com.zxinfotek.tms.core.iam.IamErrorCode;
import com.zxinfotek.tms.core.iam.api.LoginSessionService;
import com.zxinfotek.tms.core.iam.api.MemberService;
import com.zxinfotek.tms.core.iam.api.OrgReferenceProvider;
import com.zxinfotek.tms.core.iam.api.OrgService;
import com.zxinfotek.tms.core.iam.api.model.MemberCreateResultVO;
import com.zxinfotek.tms.core.iam.api.model.MemberSaveRequest;
import com.zxinfotek.tms.core.iam.api.model.OrgCreateResultVO;
import com.zxinfotek.tms.core.iam.api.model.OrgQuery;
import com.zxinfotek.tms.core.iam.api.model.OrgSaveRequest;
import com.zxinfotek.tms.core.iam.api.model.OrgTreeVO;
import com.zxinfotek.tms.core.iam.api.model.OrgVO;
import com.zxinfotek.tms.core.iam.api.model.StatusRequest;
import com.zxinfotek.tms.core.iam.entity.MemberEntity;
import com.zxinfotek.tms.core.iam.entity.OrgEntity;
import com.zxinfotek.tms.core.iam.mapper.MemberMapper;
import com.zxinfotek.tms.core.iam.mapper.OrgMapper;
import com.zxinfotek.tms.infra.context.DataScopeAssert;
import com.zxinfotek.tms.infra.context.RequestContextHolder;
import com.zxinfotek.tms.infra.excel.ExcelExportService;
import com.zxinfotek.tms.infra.i18n.I18nMessages;
import com.zxinfotek.tms.infra.mybatis.SnowflakeIdentifierGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 机构树维护：物化路径生成、上级范围校验、子机构与其管理员的同事务创建（详细设计 3.4）。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
@Service
public class OrgServiceImpl implements OrgService {

    private final OrgMapper orgMapper;
    private final MemberMapper memberMapper;
    private final MemberService memberService;
    private final LoginSessionService loginSessionService;
    private final SnowflakeIdentifierGenerator idGenerator;
    private final ExcelExportService excelExportService;
    private final List<OrgReferenceProvider> referenceProviders;

    public OrgServiceImpl(OrgMapper orgMapper,
                          MemberMapper memberMapper,
                          MemberService memberService,
                          LoginSessionService loginSessionService,
                          SnowflakeIdentifierGenerator idGenerator,
                          ExcelExportService excelExportService,
                          List<OrgReferenceProvider> referenceProviders) {
        this.orgMapper = orgMapper;
        this.memberMapper = memberMapper;
        this.memberService = memberService;
        this.loginSessionService = loginSessionService;
        this.idGenerator = idGenerator;
        this.excelExportService = excelExportService;
        this.referenceProviders = referenceProviders;
    }

    @Override
    public List<OrgTreeVO> tree() {
        List<OrgEntity> orgs = orgMapper.selectOrgScopeList(Wrappers.<OrgEntity>lambdaQuery()
                .eq(OrgEntity::getDeleted, 0)
                .orderByAsc(OrgEntity::getOrgPath));
        return buildTree(orgs);
    }

    @Override
    public List<OrgTreeVO> selector() {
        List<OrgEntity> orgs = orgMapper.selectOrgScopeList(Wrappers.<OrgEntity>lambdaQuery()
                .eq(OrgEntity::getDeleted, 0)
                .eq(OrgEntity::getStatus, EnableStatus.ENABLED)
                .orderByAsc(OrgEntity::getOrgPath));
        return buildTree(orgs);
    }

    @Override
    public PageResult<OrgVO> page(OrgQuery query) {
        Page<OrgEntity> page = new Page<>(query.resolvePageNum(), query.resolvePageSize());
        Page<OrgEntity> result = (Page<OrgEntity>) orgMapper.selectOrgPage(page, buildWrapper(query));
        List<OrgVO> rows = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        fillCounts(rows);
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), rows);
    }

    @Override
    public OrgVO detail(Long id) {
        OrgEntity org = requireOrg(id);
        DataScopeAssert.within(org.getTenantId(), org.getOrgPath());
        OrgVO vo = toVO(org);
        fillCounts(List.of(vo));
        return vo;
    }

    /**
     * 新增子机构：校验上级可管理且已启用，按上级路径拼接本级物化路径，同事务创建机构管理员。
     *
     * @author zxinfotek
     * @since 2026-09-18
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrgCreateResultVO create(OrgSaveRequest request) {
        OrgEntity parent = requireOrg(request.getParentId());
        DataScopeAssert.within(parent.getTenantId(), parent.getOrgPath());
        if (parent.getStatus() != EnableStatus.ENABLED) {
            throw new BizException(IamErrorCode.ORG_002);
        }
        assertNameUnique(parent.getId(), request.getName(), null);

        OrgEntity org = new OrgEntity();
        org.setId(idGenerator.nextId());
        org.setTenantId(parent.getTenantId());
        org.setParentId(parent.getId());
        org.setOrgPath(parent.getOrgPath() + org.getId() + "/");
        org.setOrgType(OrgType.BRANCH);
        org.setName(request.getName());
        org.setContactName(request.getContactName());
        org.setContactPhone(request.getContactPhone());
        org.setStatus(EnableStatus.ENABLED);
        org.setDeleted(0);
        orgMapper.insert(org);

        OrgCreateResultVO result = new OrgCreateResultVO();
        result.setOrgId(org.getId());
        if (request.getAdminAccount() != null && !request.getAdminAccount().isBlank()) {
            MemberSaveRequest adminRequest = new MemberSaveRequest();
            adminRequest.setOrgId(org.getId());
            adminRequest.setAccount(request.getAdminAccount());
            adminRequest.setNickname(request.getAdminNickname() == null || request.getAdminNickname().isBlank()
                    ? I18nMessages.get("msg.org.defaultAdminNickname", request.getName())
                    : request.getAdminNickname());
            adminRequest.setRoleIds(request.getAdminRoleIds());
            MemberCreateResultVO admin = memberService.create(adminRequest);
            result.setAdminMemberId(admin.getMemberId());
            result.setAdminAccount(admin.getAccount());
            result.setInitialPassword(admin.getInitialPassword());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, OrgSaveRequest request) {
        OrgEntity org = requireOrg(id);
        DataScopeAssert.within(org.getTenantId(), org.getOrgPath());
        if (org.getOrgType() == OrgType.PLATFORM) {
            throw new BizException(IamErrorCode.ORG_004, "msg.org.platformRootImmutable");
        }
        assertNameUnique(org.getParentId(), request.getName(), id);
        org.setName(request.getName());
        org.setContactName(request.getContactName());
        org.setContactPhone(request.getContactPhone());
        orgMapper.updateById(org);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, StatusRequest request) {
        OrgEntity org = requireOrg(id);
        DataScopeAssert.within(org.getTenantId(), org.getOrgPath());
        if (org.getOrgType() != OrgType.BRANCH) {
            throw new BizException(IamErrorCode.ORG_004);
        }
        org.setStatus(request.getStatus());
        orgMapper.updateById(org);
        if (request.getStatus() == EnableStatus.DISABLED) {
            loginSessionService.invalidateByOrgPath(org.getTenantId(), org.getOrgPath(),
                    SessionInvalidReason.ORG_DISABLED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        OrgEntity org = requireOrg(id);
        DataScopeAssert.within(org.getTenantId(), org.getOrgPath());
        if (org.getOrgType() != OrgType.BRANCH) {
            throw new BizException(IamErrorCode.ORG_004);
        }
        List<String> blocking = new ArrayList<>();
        Long subCount = orgMapper.selectCount(Wrappers.<OrgEntity>lambdaQuery()
                .eq(OrgEntity::getParentId, id).eq(OrgEntity::getDeleted, 0));
        if (subCount != null && subCount > 0) {
            blocking.add(I18nMessages.get("msg.block.subOrg", subCount));
        }
        Long memberCount = memberMapper.selectCount(Wrappers.<MemberEntity>lambdaQuery()
                .likeRight(MemberEntity::getOrgPath, org.getOrgPath()));
        if (memberCount != null && memberCount > 0) {
            blocking.add(I18nMessages.get("msg.block.member", memberCount));
        }
        referenceProviders.forEach(provider ->
                blocking.addAll(provider.blockingReferences(id, org.getOrgPath())));
        if (!blocking.isEmpty()) {
            throw new BizException(IamErrorCode.ORG_003, "msg.org.deleteBlocked",
                    String.join(I18nMessages.get("msg.common.separator"), blocking));
        }
        orgMapper.deleteById(id);
    }

    @Override
    public String export(OrgQuery query) {
        List<OrgEntity> orgs = orgMapper.selectOrgPage(new Page<>(1, 10000), buildWrapper(query)).getRecords();
        List<List<Object>> rows = orgs.stream().map(org -> List.<Object>of(
                text(org.getName()),
                text(orgName(org.getParentId())),
                text(I18nMessages.label(org.getOrgType())),
                text(org.getContactName()),
                text(org.getContactPhone()),
                text(I18nMessages.label(org.getStatus())),
                UtcTimes.formatCompact(org.getCreateTime()))).collect(Collectors.toList());
        return excelExportService.export("org", RequestContextHolder.get().getTenantId(),
                "export.sheet.org",
                List.of("export.column.orgName", "export.column.parentOrg", "export.column.orgType",
                        "export.column.contactName", "export.column.contactPhone", "export.column.status",
                        "export.column.createTime"),
                rows);
    }

    @Override
    public String orgName(Long orgId) {
        if (orgId == null) {
            return null;
        }
        OrgEntity org = orgMapper.selectById(orgId);
        return org == null ? null : org.getName();
    }

    @Override
    public String orgPath(Long orgId) {
        OrgEntity org = orgMapper.selectById(orgId);
        return org == null ? null : org.getOrgPath();
    }

    private List<OrgTreeVO> buildTree(List<OrgEntity> orgs) {
        Map<Long, OrgTreeVO> nodes = new LinkedHashMap<>();
        for (OrgEntity org : orgs) {
            OrgTreeVO node = new OrgTreeVO();
            node.setId(org.getId());
            node.setParentId(org.getParentId());
            node.setName(org.getName());
            node.setOrgType(org.getOrgType() == null ? null : org.getOrgType().getCode());
            node.setStatus(org.getStatus() == null ? null : org.getStatus().getCode());
            node.setOrgPath(org.getOrgPath());
            nodes.put(org.getId(), node);
        }
        List<OrgTreeVO> roots = new ArrayList<>();
        for (OrgTreeVO node : nodes.values()) {
            OrgTreeVO parent = nodes.get(node.getParentId());
            if (parent == null) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }
        // 每个节点展示该机构及下级的机构数
        for (OrgTreeVO node : nodes.values()) {
            node.setSubCount((int) nodes.values().stream()
                    .filter(other -> other.getOrgPath().startsWith(node.getOrgPath())).count());
        }
        roots.sort(Comparator.comparing(OrgTreeVO::getOrgPath));
        return roots;
    }

    private LambdaQueryWrapper<OrgEntity> buildWrapper(OrgQuery query) {
        LambdaQueryWrapper<OrgEntity> wrapper = Wrappers.<OrgEntity>lambdaQuery()
                .eq(OrgEntity::getDeleted, 0);
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            wrapper.like(OrgEntity::getName, query.getKeyword().trim());
        }
        if (query.getStatus() != null) {
            wrapper.eq(OrgEntity::getStatus, query.getStatus());
        }
        if (query.getOrgId() != null) {
            OrgEntity org = orgMapper.selectById(query.getOrgId());
            if (org != null) {
                wrapper.likeRight(OrgEntity::getOrgPath, org.getOrgPath());
            }
        }
        wrapper.orderByAsc(OrgEntity::getOrgPath);
        return wrapper;
    }

    private void assertNameUnique(Long parentId, String name, Long excludeId) {
        LambdaQueryWrapper<OrgEntity> wrapper = Wrappers.<OrgEntity>lambdaQuery()
                .eq(OrgEntity::getParentId, parentId)
                .eq(OrgEntity::getName, name);
        if (excludeId != null) {
            wrapper.ne(OrgEntity::getId, excludeId);
        }
        Long exists = orgMapper.selectCount(wrapper);
        if (exists != null && exists > 0) {
            throw new BizException(IamErrorCode.ORG_001);
        }
    }

    private void fillCounts(List<OrgVO> rows) {
        for (OrgVO row : rows) {
            Long members = memberMapper.selectCount(Wrappers.<MemberEntity>lambdaQuery()
                    .eq(MemberEntity::getOrgId, row.getId()));
            row.setMemberCount(members == null ? 0 : members.intValue());
            Long subs = orgMapper.selectCount(Wrappers.<OrgEntity>lambdaQuery()
                    .eq(OrgEntity::getParentId, row.getId()).eq(OrgEntity::getDeleted, 0));
            row.setSubOrgCount(subs == null ? 0 : subs.intValue());
        }
    }

    private OrgVO toVO(OrgEntity org) {
        OrgVO vo = new OrgVO();
        vo.setId(org.getId());
        vo.setTenantId(org.getTenantId());
        vo.setParentId(org.getParentId());
        vo.setParentName(orgName(org.getParentId()));
        vo.setOrgPath(org.getOrgPath());
        vo.setOrgType(org.getOrgType() == null ? null : org.getOrgType().getCode());
        vo.setOrgTypeLabel(I18nMessages.label(org.getOrgType()));
        vo.setName(org.getName());
        vo.setContactName(org.getContactName());
        vo.setContactPhone(org.getContactPhone());
        vo.setStatus(org.getStatus() == null ? null : org.getStatus().getCode());
        vo.setCreateTime(org.getCreateTime());
        return vo;
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }

    private OrgEntity requireOrg(Long id) {
        if (id == null) {
            throw new BizException(IamErrorCode.ORG_002, "msg.org.parentRequired");
        }
        OrgEntity org = orgMapper.selectById(id);
        if (org == null) {
            throw new NotFoundException("msg.org.notFound");
        }
        return org;
    }

}

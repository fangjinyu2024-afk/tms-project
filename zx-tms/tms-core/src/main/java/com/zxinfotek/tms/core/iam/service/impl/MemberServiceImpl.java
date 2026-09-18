package com.zxinfotek.tms.core.iam.service.impl;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zxinfotek.tms.common.enums.EnableStatus;
import com.zxinfotek.tms.common.enums.SessionInvalidReason;
import com.zxinfotek.tms.common.exception.BizException;
import com.zxinfotek.tms.common.exception.NotFoundException;
import com.zxinfotek.tms.common.model.PageResult;
import com.zxinfotek.tms.core.iam.IamErrorCode;
import com.zxinfotek.tms.core.iam.api.LoginSessionService;
import com.zxinfotek.tms.core.iam.api.MemberService;
import com.zxinfotek.tms.core.iam.api.RoleService;
import com.zxinfotek.tms.core.iam.api.model.AssignRoleRequest;
import com.zxinfotek.tms.core.iam.api.model.MemberCreateResultVO;
import com.zxinfotek.tms.core.iam.api.model.MemberQuery;
import com.zxinfotek.tms.core.iam.api.model.MemberSaveRequest;
import com.zxinfotek.tms.core.iam.api.model.MemberVO;
import com.zxinfotek.tms.core.iam.api.model.RoleOptionVO;
import com.zxinfotek.tms.core.iam.api.model.StatusRequest;
import com.zxinfotek.tms.core.iam.entity.MemberEntity;
import com.zxinfotek.tms.core.iam.entity.MemberRoleEntity;
import com.zxinfotek.tms.core.iam.entity.OrgEntity;
import com.zxinfotek.tms.core.iam.entity.RoleEntity;
import com.zxinfotek.tms.core.iam.mapper.MemberMapper;
import com.zxinfotek.tms.core.iam.mapper.MemberRoleMapper;
import com.zxinfotek.tms.core.iam.mapper.OrgMapper;
import com.zxinfotek.tms.core.iam.mapper.RoleMapper;
import com.zxinfotek.tms.core.iam.service.support.PasswordGenerator;
import com.zxinfotek.tms.infra.context.DataScopeAssert;
import com.zxinfotek.tms.infra.context.RequestContextHolder;
import com.zxinfotek.tms.infra.excel.ExcelExportService;
import com.zxinfotek.tms.infra.lock.DistributedLock;
import lombok.Data;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 成员账号维护：账号唯一性、所属机构范围、角色分配与账号生命周期（详细设计 3.5）。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
@Service
public class MemberServiceImpl implements MemberService {

    private static final Pattern ACCOUNT_PATTERN = Pattern.compile("^[A-Za-z0-9._-]{3,64}$");

    private final MemberMapper memberMapper;
    private final MemberRoleMapper memberRoleMapper;
    private final RoleMapper roleMapper;
    private final OrgMapper orgMapper;
    private final RoleService roleService;
    private final LoginSessionService loginSessionService;
    private final AdminProtection adminProtection;
    private final PasswordEncoder passwordEncoder;
    private final DistributedLock distributedLock;
    private final ExcelExportService excelExportService;

    public MemberServiceImpl(MemberMapper memberMapper,
                             MemberRoleMapper memberRoleMapper,
                             RoleMapper roleMapper,
                             OrgMapper orgMapper,
                             RoleService roleService,
                             LoginSessionService loginSessionService,
                             AdminProtection adminProtection,
                             PasswordEncoder passwordEncoder,
                             DistributedLock distributedLock,
                             ExcelExportService excelExportService) {
        this.memberMapper = memberMapper;
        this.memberRoleMapper = memberRoleMapper;
        this.roleMapper = roleMapper;
        this.orgMapper = orgMapper;
        this.roleService = roleService;
        this.loginSessionService = loginSessionService;
        this.adminProtection = adminProtection;
        this.passwordEncoder = passwordEncoder;
        this.distributedLock = distributedLock;
        this.excelExportService = excelExportService;
    }

    @Override
    public PageResult<MemberVO> page(MemberQuery query) {
        Page<MemberEntity> page = new Page<>(query.resolvePageNum(), query.resolvePageSize());
        Page<MemberEntity> result = (Page<MemberEntity>) memberMapper.selectMemberPage(page, buildWrapper(query));
        List<MemberVO> rows = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        fillRoles(rows);
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), rows);
    }

    @Override
    public MemberVO detail(Long id) {
        MemberEntity member = requireMember(id);
        DataScopeAssert.within(member.getTenantId(), member.getOrgPath());
        MemberVO vo = toVO(member);
        fillRoles(List.of(vo));
        return vo;
    }

    /**
     * 新增成员：校验账号与机构范围、校验角色可分配，生成随机初始密码并置强制改密标记。
     *
     * @author zxinfotek
     * @since 2026-09-18
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberCreateResultVO create(MemberSaveRequest request) {
        OrgEntity org = requireOrg(request.getOrgId());
        DataScopeAssert.within(org.getTenantId(), org.getOrgPath());
        if (org.getStatus() != EnableStatus.ENABLED) {
            throw new BizException(IamErrorCode.ORG_002, "所属机构已停用");
        }
        String account = normalizeAccount(request.getAccount());
        assertAccountAvailable(account);
        assertEmailAvailable(request.getEmail(), null);
        roleService.assertAssignable(request.getRoleIds(), org.getTenantId(), org.getOrgPath());

        String initialPassword = PasswordGenerator.generate();
        MemberEntity member = new MemberEntity();
        member.setTenantId(org.getTenantId());
        member.setOrgId(org.getId());
        member.setOrgPath(org.getOrgPath());
        member.setAccount(request.getAccount().trim());
        member.setAccountLower(account);
        member.setNickname(request.getNickname());
        member.setPasswordHash(passwordEncoder.encode(initialPassword));
        member.setEmail(emptyToNull(request.getEmail()));
        member.setEmailVerified(0);
        member.setPhone(emptyToNull(request.getPhone()));
        member.setMustChangePassword(1);
        member.setFailCount(0);
        member.setPermVersion(1);
        member.setStatus(EnableStatus.ENABLED);
        member.setDeleted(0);
        memberMapper.insert(member);
        replaceRoles(member.getId(), request.getRoleIds());

        MemberCreateResultVO result = new MemberCreateResultVO();
        result.setMemberId(member.getId());
        result.setAccount(member.getAccount());
        result.setInitialPassword(initialPassword);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, MemberSaveRequest request) {
        MemberEntity member = requireMember(id);
        DataScopeAssert.within(member.getTenantId(), member.getOrgPath());
        assertEmailAvailable(request.getEmail(), id);
        roleService.assertAssignable(request.getRoleIds(), member.getTenantId(), member.getOrgPath());

        boolean emailChanged = request.getEmail() != null
                && !request.getEmail().equalsIgnoreCase(member.getEmail());
        member.setNickname(request.getNickname());
        member.setEmail(emptyToNull(request.getEmail()));
        if (emailChanged) {
            member.setEmailVerified(0);
        }
        member.setPhone(emptyToNull(request.getPhone()));
        memberMapper.updateById(member);
        replaceRoles(id, request.getRoleIds());
        adminProtection.assertAdminRemains(member.getOrgId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, StatusRequest request) {
        MemberEntity member = requireMember(id);
        DataScopeAssert.within(member.getTenantId(), member.getOrgPath());
        member.setStatus(request.getStatus());
        memberMapper.updateById(member);
        if (request.getStatus() == EnableStatus.DISABLED) {
            adminProtection.assertAdminRemains(member.getOrgId());
            loginSessionService.invalidateByMember(id, SessionInvalidReason.ACCOUNT_DISABLED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        MemberEntity member = requireMember(id);
        DataScopeAssert.within(member.getTenantId(), member.getOrgPath());
        if (id.equals(RequestContextHolder.get().getMemberId())) {
            throw new BizException(IamErrorCode.MEMBER_005, "不能删除当前登录账号");
        }
        memberMapper.deleteById(id);
        memberRoleMapper.delete(Wrappers.<MemberRoleEntity>lambdaQuery().eq(MemberRoleEntity::getMemberId, id));
        adminProtection.assertAdminRemains(member.getOrgId());
        loginSessionService.invalidateByMember(id, SessionInvalidReason.MEMBER_DELETED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberCreateResultVO resetPassword(Long id) {
        MemberEntity member = requireMember(id);
        DataScopeAssert.within(member.getTenantId(), member.getOrgPath());
        String initialPassword = PasswordGenerator.generate();
        member.setPasswordHash(passwordEncoder.encode(initialPassword));
        member.setMustChangePassword(1);
        member.setPasswordUpdatedAt(com.zxinfotek.tms.common.util.UtcTimes.now());
        member.setFailCount(0);
        member.setLockedUntil(null);
        memberMapper.updateById(member);
        loginSessionService.invalidateByMember(id, SessionInvalidReason.PASSWORD_RESET);

        MemberCreateResultVO result = new MemberCreateResultVO();
        result.setMemberId(id);
        result.setAccount(member.getAccount());
        result.setInitialPassword(initialPassword);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long id, AssignRoleRequest request) {
        MemberEntity member = requireMember(id);
        DataScopeAssert.within(member.getTenantId(), member.getOrgPath());
        roleService.assertAssignable(request.getRoleIds(), member.getTenantId(), member.getOrgPath());
        replaceRoles(id, request.getRoleIds());
        adminProtection.assertAdminRemains(member.getOrgId());
    }

    @Override
    public String export(MemberQuery query) {
        List<MemberEntity> members = memberMapper
                .selectMemberPage(new Page<>(1, 10000), buildWrapper(query)).getRecords();
        List<MemberExportRow> rows = members.stream().map(member -> {
            MemberExportRow row = new MemberExportRow();
            row.setAccount(member.getAccount());
            row.setNickname(member.getNickname());
            row.setOrgName(orgName(member.getOrgId()));
            row.setEmail(com.zxinfotek.tms.common.util.MaskUtils.maskEmail(member.getEmail()));
            row.setPhone(com.zxinfotek.tms.common.util.MaskUtils.maskPhone(member.getPhone()));
            row.setStatus(member.getStatus() == null ? "" : member.getStatus().getLabel());
            row.setCreateTime(member.getCreateTime());
            return row;
        }).collect(Collectors.toList());
        return excelExportService.export("member", RequestContextHolder.get().getTenantId(),
                "成员", MemberExportRow.class, rows);
    }

    /** 角色分配为全量覆盖，按成员维度加锁后先删后插，避免并发分配产生重复关系。 */
    private void replaceRoles(Long memberId, List<Long> roleIds) {
        distributedLock.executeInLock("member-role:" + memberId, Duration.ofSeconds(10),
                Duration.ofSeconds(3), () -> {
                    memberRoleMapper.delete(Wrappers.<MemberRoleEntity>lambdaQuery()
                            .eq(MemberRoleEntity::getMemberId, memberId));
                    for (Long roleId : new HashSet<>(roleIds)) {
                        MemberRoleEntity entity = new MemberRoleEntity();
                        entity.setMemberId(memberId);
                        entity.setRoleId(roleId);
                        memberRoleMapper.insert(entity);
                    }
                    memberMapper.increasePermVersion(memberId);
                    return null;
                });
    }

    private void assertAccountAvailable(String accountLower) {
        if (!ACCOUNT_PATTERN.matcher(accountLower).matches()) {
            throw new BizException(IamErrorCode.MEMBER_002, "账号为 3 至 64 位字母、数字、点、下划线或短横线");
        }
        Long exists = memberMapper.selectCount(Wrappers.<MemberEntity>lambdaQuery()
                .eq(MemberEntity::getAccountLower, accountLower));
        if (exists != null && exists > 0) {
            throw new BizException(IamErrorCode.MEMBER_001);
        }
    }

    private void assertEmailAvailable(String email, Long excludeId) {
        if (email == null || email.isBlank()) {
            return;
        }
        LambdaQueryWrapper<MemberEntity> wrapper = Wrappers.<MemberEntity>lambdaQuery()
                .eq(MemberEntity::getEmail, email.trim());
        if (excludeId != null) {
            wrapper.ne(MemberEntity::getId, excludeId);
        }
        Long exists = memberMapper.selectCount(wrapper);
        if (exists != null && exists > 0) {
            throw new BizException(IamErrorCode.MEMBER_003);
        }
    }

    private LambdaQueryWrapper<MemberEntity> buildWrapper(MemberQuery query) {
        LambdaQueryWrapper<MemberEntity> wrapper = Wrappers.<MemberEntity>lambdaQuery()
                .eq(MemberEntity::getDeleted, 0);
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            String keyword = query.getKeyword().trim();
            wrapper.and(inner -> inner.like(MemberEntity::getAccount, keyword)
                    .or().like(MemberEntity::getNickname, keyword));
        }
        if (query.getStatus() != null) {
            wrapper.eq(MemberEntity::getStatus, query.getStatus());
        }
        if (query.getOrgId() != null) {
            OrgEntity org = orgMapper.selectById(query.getOrgId());
            if (org != null) {
                wrapper.likeRight(MemberEntity::getOrgPath, org.getOrgPath());
            }
        }
        wrapper.orderByDesc(MemberEntity::getCreateTime);
        return wrapper;
    }

    private void fillRoles(List<MemberVO> rows) {
        if (rows.isEmpty()) {
            return;
        }
        List<Long> memberIds = rows.stream().map(MemberVO::getId).collect(Collectors.toList());
        List<MemberRoleEntity> relations = memberRoleMapper.selectList(
                Wrappers.<MemberRoleEntity>lambdaQuery().in(MemberRoleEntity::getMemberId, memberIds));
        if (relations.isEmpty()) {
            rows.forEach(row -> row.setRoles(new ArrayList<>()));
            return;
        }
        List<Long> roleIds = relations.stream().map(MemberRoleEntity::getRoleId).distinct()
                .collect(Collectors.toList());
        Map<Long, RoleEntity> roles = roleMapper.selectBatchIds(roleIds).stream()
                .collect(Collectors.toMap(RoleEntity::getId, role -> role));
        Map<Long, List<Long>> roleIdsByMember = relations.stream()
                .collect(Collectors.groupingBy(MemberRoleEntity::getMemberId,
                        Collectors.mapping(MemberRoleEntity::getRoleId, Collectors.toList())));
        for (MemberVO row : rows) {
            List<RoleOptionVO> options = new ArrayList<>();
            for (Long roleId : roleIdsByMember.getOrDefault(row.getId(), List.of())) {
                RoleEntity role = roles.get(roleId);
                if (role == null) {
                    continue;
                }
                RoleOptionVO option = new RoleOptionVO();
                option.setId(role.getId());
                option.setName(role.getName());
                option.setBuiltin(role.getBuiltinCode() != null);
                option.setDataScope(role.getDataScope() == null ? null : role.getDataScope().getCode());
                options.add(option);
            }
            row.setRoles(options);
        }
    }

    private MemberVO toVO(MemberEntity member) {
        MemberVO vo = new MemberVO();
        vo.setId(member.getId());
        vo.setTenantId(member.getTenantId());
        vo.setOrgId(member.getOrgId());
        vo.setOrgName(orgName(member.getOrgId()));
        vo.setOrgPath(member.getOrgPath());
        vo.setAccount(member.getAccount());
        vo.setNickname(member.getNickname());
        vo.setEmail(member.getEmail());
        vo.setEmailVerified(member.getEmailVerified() != null && member.getEmailVerified() == 1);
        vo.setPhone(member.getPhone());
        vo.setStatus(member.getStatus() == null ? null : member.getStatus().getCode());
        vo.setMustChangePassword(member.getMustChangePassword() != null && member.getMustChangePassword() == 1);
        vo.setLastPasswordTime(member.getPasswordUpdatedAt());
        vo.setCreateTime(member.getCreateTime());
        return vo;
    }

    private String orgName(Long orgId) {
        OrgEntity org = orgMapper.selectById(orgId);
        return org == null ? null : org.getName();
    }

    private OrgEntity requireOrg(Long orgId) {
        if (orgId == null) {
            throw new BizException(IamErrorCode.ORG_002, "请选择所属机构");
        }
        OrgEntity org = orgMapper.selectById(orgId);
        if (org == null) {
            throw new NotFoundException("机构不存在");
        }
        return org;
    }

    private MemberEntity requireMember(Long id) {
        MemberEntity member = memberMapper.selectById(id);
        if (member == null) {
            throw new NotFoundException("成员不存在");
        }
        return member;
    }

    private static String normalizeAccount(String account) {
        if (account == null || account.isBlank()) {
            throw new BizException(IamErrorCode.MEMBER_002, "请输入登录账号");
        }
        return account.trim().toLowerCase();
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    @Data
    public static class MemberExportRow {
        @ExcelProperty("登录账号")
        private String account;
        @ExcelProperty("昵称")
        private String nickname;
        @ExcelProperty("所属机构")
        private String orgName;
        @ExcelProperty("邮箱")
        private String email;
        @ExcelProperty("联系电话")
        private String phone;
        @ExcelProperty("状态")
        private String status;
        @ExcelProperty("创建时间")
        private LocalDateTime createTime;
    }
}

package com.zxinfotek.tms.admin.init;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zxinfotek.tms.common.enums.EnableStatus;
import com.zxinfotek.tms.core.iam.api.PermissionService;
import com.zxinfotek.tms.core.iam.api.RoleService;
import com.zxinfotek.tms.core.iam.api.TenantService;
import com.zxinfotek.tms.core.iam.constant.IamConstants;
import com.zxinfotek.tms.core.iam.entity.MemberEntity;
import com.zxinfotek.tms.core.iam.entity.MemberRoleEntity;
import com.zxinfotek.tms.core.iam.enums.BuiltinRole;
import com.zxinfotek.tms.core.iam.mapper.MemberMapper;
import com.zxinfotek.tms.core.iam.mapper.MemberRoleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 服务启动初始化：同步权限目录、初始化平台租户与内置平台管理员角色及首个平台账号。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
@Component
public class SystemInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SystemInitializer.class);

    private final PermissionService permissionService;
    private final TenantService tenantService;
    private final RoleService roleService;
    private final MemberMapper memberMapper;
    private final MemberRoleMapper memberRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final String adminAccount;
    private final String adminPassword;

    public SystemInitializer(PermissionService permissionService,
                             TenantService tenantService,
                             RoleService roleService,
                             MemberMapper memberMapper,
                             MemberRoleMapper memberRoleMapper,
                             PasswordEncoder passwordEncoder,
                             @Value("${tms.init.admin-account:admin}") String adminAccount,
                             @Value("${tms.init.admin-password:Tms@12345}") String adminPassword) {
        this.permissionService = permissionService;
        this.tenantService = tenantService;
        this.roleService = roleService;
        this.memberMapper = memberMapper;
        this.memberRoleMapper = memberRoleMapper;
        this.passwordEncoder = passwordEncoder;
        this.adminAccount = adminAccount;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        permissionService.syncCatalog();
        tenantService.ensurePlatformTenant();
        Long roleId = roleService.ensureBuiltinRole(IamConstants.PLATFORM_TENANT_ID,
                IamConstants.PLATFORM_ROOT_ORG_ID, IamConstants.PLATFORM_ORG_PATH,
                BuiltinRole.PLATFORM_ADMIN);
        int changed = roleService.syncBuiltinRolePermissions();
        if (changed > 0) {
            log.info("已按权限目录同步 {} 个内置角色的权限码", changed);
        }
        ensurePlatformAdmin(roleId);
    }

    /** 首次启动创建平台管理员账号，初始密码来自配置且强制首登修改，不写入日志。 */
    private void ensurePlatformAdmin(Long roleId) {
        String accountLower = adminAccount.trim().toLowerCase();
        MemberEntity existing = memberMapper.selectOne(Wrappers.<MemberEntity>lambdaQuery()
                .eq(MemberEntity::getAccountLower, accountLower).last("LIMIT 1"));
        if (existing != null) {
            return;
        }
        MemberEntity member = new MemberEntity();
        member.setTenantId(IamConstants.PLATFORM_TENANT_ID);
        member.setOrgId(IamConstants.PLATFORM_ROOT_ORG_ID);
        member.setOrgPath(IamConstants.PLATFORM_ORG_PATH);
        member.setAccount(adminAccount.trim());
        member.setAccountLower(accountLower);
        member.setNickname("平台管理员");
        member.setPasswordHash(passwordEncoder.encode(adminPassword));
        member.setEmailVerified(0);
        member.setMustChangePassword(1);
        member.setFailCount(0);
        member.setPermVersion(1);
        member.setStatus(EnableStatus.ENABLED);
        member.setDeleted(0);
        memberMapper.insert(member);

        MemberRoleEntity relation = new MemberRoleEntity();
        relation.setMemberId(member.getId());
        relation.setRoleId(roleId);
        memberRoleMapper.insert(relation);
        log.info("已创建平台管理员账号 {}，首次登录须修改初始密码", member.getAccount());
    }
}

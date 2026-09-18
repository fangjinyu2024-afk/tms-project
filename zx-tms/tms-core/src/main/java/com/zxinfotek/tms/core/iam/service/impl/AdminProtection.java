package com.zxinfotek.tms.core.iam.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zxinfotek.tms.common.enums.EnableStatus;
import com.zxinfotek.tms.common.exception.BizException;
import com.zxinfotek.tms.core.iam.IamErrorCode;
import com.zxinfotek.tms.core.iam.api.PermissionService;
import com.zxinfotek.tms.core.iam.entity.MemberEntity;
import com.zxinfotek.tms.core.iam.mapper.MemberMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 管理能力保护：任何改动之后，机构内必须仍有至少一名启用成员持有成员维护权限。
 *
 * <p>对应业务规则「不得移除本机构最后一名管理员的必要管理权限」（详细设计 3.2.5 第 13 条、3.5.5 第 8 条）。</p>
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
@Component
public class AdminProtection {

    /** 必要管理权限：能够维护本机构成员及其角色 */
    private static final Set<String> CRITICAL_PERMS = Set.of("members:edit", "members:assign");

    private final MemberMapper memberMapper;
    private final PermissionService permissionService;

    public AdminProtection(MemberMapper memberMapper, PermissionService permissionService) {
        this.memberMapper = memberMapper;
        this.permissionService = permissionService;
    }

    /**
     * 在改动生效后校验机构仍有管理员，校验不通过抛出业务异常并回滚本次事务。
     *
     * @author zxinfotek
     * @since 2026-09-18
     */
    public void assertAdminRemains(Long orgId) {
        if (orgId == null) {
            return;
        }
        List<MemberEntity> members = memberMapper.selectList(Wrappers.<MemberEntity>lambdaQuery()
                .eq(MemberEntity::getOrgId, orgId)
                .eq(MemberEntity::getStatus, EnableStatus.ENABLED));
        for (MemberEntity member : members) {
            Set<String> permissions = permissionService.load(member.getId()).permissions().keySet();
            if (permissions.containsAll(CRITICAL_PERMS)) {
                return;
            }
        }
        throw new BizException(IamErrorCode.MEMBER_005);
    }
}

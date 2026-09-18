package com.zxinfotek.tms.core.iam.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.zxinfotek.tms.core.iam.entity.RoleEntity;
import com.zxinfotek.tms.infra.mybatis.DataScope;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<RoleEntity> {

    @DataScope(orgIdColumn = "owner_org_id", orgPathColumn = "owner_org_path")
    @Select("SELECT * FROM t_role ${ew.customSqlSegment}")
    IPage<RoleEntity> selectRolePage(IPage<RoleEntity> page,
                                     @Param(Constants.WRAPPER) Wrapper<RoleEntity> wrapper);

    /**
     * 目标机构可分配的角色：归属机构为目标机构本级或其任一上级，且同一租户。
     *
     * @author zxinfotek
     * @since 2026-09-18
     */
    @Select("SELECT * FROM t_role WHERE deleted = 0 AND tenant_id = #{tenantId} "
            + "AND #{orgPath} LIKE CONCAT(owner_org_path, '%') ORDER BY owner_org_path, name")
    List<RoleEntity> selectAssignableRoles(@Param("tenantId") Long tenantId,
                                           @Param("orgPath") String orgPath);

    @Update("UPDATE t_role SET perm_version = perm_version + 1 WHERE id = #{roleId}")
    int increasePermVersion(@Param("roleId") Long roleId);
}

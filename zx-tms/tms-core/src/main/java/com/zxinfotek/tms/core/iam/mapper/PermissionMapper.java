package com.zxinfotek.tms.core.iam.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zxinfotek.tms.core.iam.entity.PermissionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PermissionMapper extends BaseMapper<PermissionEntity> {

    /**
     * 权限目录同步：按权限码 upsert，重复启动结果一致（详细设计 3.2.7）。
     *
     * @author zxinfotek
     * @since 2026-09-18
     */
    @InterceptorIgnore(dataPermission = "true")
    @Update("INSERT INTO t_permission (id, perm_code, menu_key, menu_name, group_name, action, action_name, "
            + "platform_only, sort, status) VALUES (#{id}, #{permCode}, #{menuKey}, #{menuName}, #{groupName}, "
            + "#{action}, #{actionName}, #{platformOnly}, #{sort}, 'ENABLED') "
            + "ON DUPLICATE KEY UPDATE menu_key = #{menuKey}, menu_name = #{menuName}, group_name = #{groupName}, "
            + "action = #{action}, action_name = #{actionName}, platform_only = #{platformOnly}, "
            + "sort = #{sort}, status = 'ENABLED'")
    int upsert(@Param("id") Long id,
               @Param("permCode") String permCode,
               @Param("menuKey") String menuKey,
               @Param("menuName") String menuName,
               @Param("groupName") String groupName,
               @Param("action") String action,
               @Param("actionName") String actionName,
               @Param("platformOnly") Integer platformOnly,
               @Param("sort") Integer sort);
}

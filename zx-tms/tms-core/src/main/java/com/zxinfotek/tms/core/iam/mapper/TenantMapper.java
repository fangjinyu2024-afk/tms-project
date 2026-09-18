package com.zxinfotek.tms.core.iam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zxinfotek.tms.core.iam.entity.TenantEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface TenantMapper extends BaseMapper<TenantEntity> {

    @Update("UPDATE t_tenant SET feature_version = feature_version + 1 WHERE id = #{tenantId}")
    int increaseFeatureVersion(@Param("tenantId") Long tenantId);
}

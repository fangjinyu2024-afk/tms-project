package com.zxinfotek.tms.core.iam.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.zxinfotek.tms.core.iam.entity.OrgEntity;
import com.zxinfotek.tms.infra.mybatis.DataScope;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrgMapper extends BaseMapper<OrgEntity> {

    @DataScope(orgIdColumn = "id")
    @Select("SELECT * FROM t_org ${ew.customSqlSegment}")
    IPage<OrgEntity> selectOrgPage(IPage<OrgEntity> page,
                                   @Param(Constants.WRAPPER) Wrapper<OrgEntity> wrapper);

    @DataScope(orgIdColumn = "id")
    @Select("SELECT * FROM t_org ${ew.customSqlSegment}")
    List<OrgEntity> selectOrgScopeList(@Param(Constants.WRAPPER) Wrapper<OrgEntity> wrapper);
}

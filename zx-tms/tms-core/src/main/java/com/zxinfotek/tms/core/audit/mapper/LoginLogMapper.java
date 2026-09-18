package com.zxinfotek.tms.core.audit.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.zxinfotek.tms.core.audit.entity.LoginLogEntity;
import com.zxinfotek.tms.infra.mybatis.DataScope;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLogEntity> {

    @DataScope
    @Select("SELECT * FROM t_login_log ${ew.customSqlSegment}")
    IPage<LoginLogEntity> selectLoginLogPage(IPage<LoginLogEntity> page,
                                             @Param(Constants.WRAPPER) Wrapper<LoginLogEntity> wrapper);

    @DataScope
    @Select("SELECT * FROM t_login_log ${ew.customSqlSegment}")
    List<LoginLogEntity> selectLoginLogScopeList(@Param(Constants.WRAPPER) Wrapper<LoginLogEntity> wrapper);
}

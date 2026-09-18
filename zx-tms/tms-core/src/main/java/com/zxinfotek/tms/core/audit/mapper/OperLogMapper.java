package com.zxinfotek.tms.core.audit.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.zxinfotek.tms.core.audit.entity.OperLogEntity;
import com.zxinfotek.tms.infra.mybatis.DataScope;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OperLogMapper extends BaseMapper<OperLogEntity> {

    @DataScope
    @Select("SELECT * FROM t_oper_log ${ew.customSqlSegment}")
    IPage<OperLogEntity> selectOperLogPage(IPage<OperLogEntity> page,
                                           @Param(Constants.WRAPPER) Wrapper<OperLogEntity> wrapper);

    @DataScope
    @Select("SELECT * FROM t_oper_log ${ew.customSqlSegment}")
    List<OperLogEntity> selectOperLogScopeList(@Param(Constants.WRAPPER) Wrapper<OperLogEntity> wrapper);
}

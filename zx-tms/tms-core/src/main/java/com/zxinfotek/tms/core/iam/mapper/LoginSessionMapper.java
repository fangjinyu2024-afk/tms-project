package com.zxinfotek.tms.core.iam.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.zxinfotek.tms.core.iam.entity.LoginSessionEntity;
import com.zxinfotek.tms.infra.mybatis.DataScope;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LoginSessionMapper extends BaseMapper<LoginSessionEntity> {

    @DataScope
    @Select("SELECT * FROM t_login_session ${ew.customSqlSegment}")
    IPage<LoginSessionEntity> selectSessionPage(IPage<LoginSessionEntity> page,
                                                @Param(Constants.WRAPPER) Wrapper<LoginSessionEntity> wrapper);

    @DataScope
    @Select("SELECT * FROM t_login_session ${ew.customSqlSegment}")
    List<LoginSessionEntity> selectSessionScopeList(
            @Param(Constants.WRAPPER) Wrapper<LoginSessionEntity> wrapper);
}

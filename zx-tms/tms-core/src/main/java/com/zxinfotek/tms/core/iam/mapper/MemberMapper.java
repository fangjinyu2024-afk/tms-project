package com.zxinfotek.tms.core.iam.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.zxinfotek.tms.core.iam.entity.MemberEntity;
import com.zxinfotek.tms.infra.mybatis.DataScope;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface MemberMapper extends BaseMapper<MemberEntity> {

    @DataScope
    @Select("SELECT * FROM t_member ${ew.customSqlSegment}")
    IPage<MemberEntity> selectMemberPage(IPage<MemberEntity> page,
                                         @Param(Constants.WRAPPER) Wrapper<MemberEntity> wrapper);

    @DataScope
    @Select("SELECT * FROM t_member ${ew.customSqlSegment}")
    List<MemberEntity> selectMemberScopeList(@Param(Constants.WRAPPER) Wrapper<MemberEntity> wrapper);

    @Update("UPDATE t_member SET perm_version = perm_version + 1 WHERE id = #{memberId}")
    int increasePermVersion(@Param("memberId") Long memberId);
}

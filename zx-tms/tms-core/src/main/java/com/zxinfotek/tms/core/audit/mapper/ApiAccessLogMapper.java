package com.zxinfotek.tms.core.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zxinfotek.tms.core.audit.entity.ApiAccessLogEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ApiAccessLogMapper extends BaseMapper<ApiAccessLogEntity> {
}

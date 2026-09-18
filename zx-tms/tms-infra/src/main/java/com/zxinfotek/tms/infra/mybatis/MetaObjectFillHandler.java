package com.zxinfotek.tms.infra.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.zxinfotek.tms.common.util.UtcTimes;
import com.zxinfotek.tms.infra.context.RequestContext;
import com.zxinfotek.tms.infra.context.RequestContextHolder;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MetaObjectFillHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = UtcTimes.now();
        RequestContext context = RequestContextHolder.get();
        strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        strictInsertFill(metaObject, "createBy", Long.class, context.getMemberId());
        strictInsertFill(metaObject, "createByName", String.class, context.getNickname());
        strictInsertFill(metaObject, "updateBy", Long.class, context.getMemberId());
        strictInsertFill(metaObject, "updateByName", String.class, context.getNickname());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        RequestContext context = RequestContextHolder.get();
        strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, UtcTimes.now());
        strictUpdateFill(metaObject, "updateBy", Long.class, context.getMemberId());
        strictUpdateFill(metaObject, "updateByName", String.class, context.getNickname());
    }
}

package com.zxinfotek.tms.infra.mybatis;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.zxinfotek.tms.common.id.SnowflakeIdGenerator;

public class SnowflakeIdentifierGenerator implements IdentifierGenerator {

    private final SnowflakeIdGenerator delegate;

    public SnowflakeIdentifierGenerator(long workerId, long datacenterId) {
        this.delegate = new SnowflakeIdGenerator(workerId, datacenterId);
    }

    @Override
    public Number nextId(Object entity) {
        return delegate.nextId();
    }

    public long nextId() {
        return delegate.nextId();
    }
}

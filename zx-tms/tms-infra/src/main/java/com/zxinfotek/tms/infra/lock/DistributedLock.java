package com.zxinfotek.tms.infra.lock;

import com.zxinfotek.tms.common.util.IdUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis 分布式锁，用于权限重载、角色分配等按维度串行化的场景（详细设计 7.8）。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
@Component
public class DistributedLock {

    private static final String KEY_PREFIX = "tms:lock:";

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    private final StringRedisTemplate stringRedisTemplate;

    public DistributedLock(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public String tryLock(String name, Duration ttl) {
        String token = IdUtils.randomHex(8);
        Boolean acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(KEY_PREFIX + name, token, ttl);
        return Boolean.TRUE.equals(acquired) ? token : null;
    }

    public void unlock(String name, String token) {
        if (token == null) {
            return;
        }
        stringRedisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(KEY_PREFIX + name), token);
    }

    /**
     * 在锁内执行；未抢到锁时按固定间隔重试，超时后直接执行以避免请求堆积。
     *
     * @author zxinfotek
     * @since 2026-09-18
     */
    public <T> T executeInLock(String name, Duration ttl, Duration waitTime, Supplier<T> action) {
        long deadline = System.currentTimeMillis() + waitTime.toMillis();
        String token = tryLock(name, ttl);
        while (token == null && System.currentTimeMillis() < deadline) {
            try {
                TimeUnit.MILLISECONDS.sleep(20L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            token = tryLock(name, ttl);
        }
        try {
            return action.get();
        } finally {
            unlock(name, token);
        }
    }
}

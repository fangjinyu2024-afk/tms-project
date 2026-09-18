package com.zxinfotek.tms.infra.security;

import com.zxinfotek.tms.common.util.IdUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 会话令牌生成与哈希：服务端只保存 SHA-256 摘要，不存明文令牌（详细设计 3.1.5 第 1 条）。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
public final class TokenHasher {

    private static final SecureRandom RANDOM = new SecureRandom();

    private TokenHasher() {
    }

    public static String newToken() {
        byte[] buf = new byte[32];
        RANDOM.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

    public static String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return IdUtils.toHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("当前运行环境缺少 SHA-256 实现", e);
        }
    }
}

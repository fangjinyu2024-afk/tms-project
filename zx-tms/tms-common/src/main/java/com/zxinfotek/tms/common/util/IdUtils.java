package com.zxinfotek.tms.common.util;

import java.security.SecureRandom;
import java.util.UUID;

public final class IdUtils {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private IdUtils() {
    }

    /** 32 位十六进制关联编号，串联接口访问日志、操作日志与设备报文记录（详细设计 7.5）。 */
    public static String traceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String randomHex(int bytes) {
        byte[] buf = new byte[bytes];
        RANDOM.nextBytes(buf);
        return toHex(buf);
    }

    public static String toHex(byte[] bytes) {
        char[] chars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            chars[i * 2] = HEX[(bytes[i] >> 4) & 0x0F];
            chars[i * 2 + 1] = HEX[bytes[i] & 0x0F];
        }
        return new String(chars);
    }
}

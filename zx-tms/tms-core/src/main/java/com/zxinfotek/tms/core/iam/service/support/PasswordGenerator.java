package com.zxinfotek.tms.core.iam.service.support;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 初始密码生成：创建成员与重置密码时由系统生成，只在响应中返回一次，不落库明文。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
public final class PasswordGenerator {

    private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijkmnpqrstuvwxyz";
    private static final String DIGIT = "23456789";
    private static final String SYMBOL = "@#$%&*";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int LENGTH = 12;

    private PasswordGenerator() {
    }

    public static String generate() {
        List<Character> chars = new ArrayList<>(LENGTH);
        chars.add(pick(UPPER));
        chars.add(pick(LOWER));
        chars.add(pick(DIGIT));
        chars.add(pick(SYMBOL));
        String all = UPPER + LOWER + DIGIT + SYMBOL;
        while (chars.size() < LENGTH) {
            chars.add(pick(all));
        }
        Collections.shuffle(chars, RANDOM);
        StringBuilder builder = new StringBuilder(LENGTH);
        chars.forEach(builder::append);
        return builder.toString();
    }

    private static char pick(String source) {
        return source.charAt(RANDOM.nextInt(source.length()));
    }
}

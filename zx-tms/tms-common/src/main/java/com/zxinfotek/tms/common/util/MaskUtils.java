package com.zxinfotek.tms.common.util;

/**
 * 脱敏工具，敏感内容禁止落库、写日志或出现在接口响应与导出文件中（详细设计 7.6）。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
public final class MaskUtils {

    private MaskUtils() {
    }

    /** 只保留末 4 位，用于授权码一类不可回显的凭据。 */
    public static String keepTail4(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        if (value.length() <= 4) {
            return "****";
        }
        return "****" + value.substring(value.length() - 4);
    }

    public static String maskEmail(String email) {
        if (email == null || email.indexOf('@') < 1) {
            return email;
        }
        int at = email.indexOf('@');
        String name = email.substring(0, at);
        String kept = name.length() <= 2 ? name.substring(0, 1) : name.substring(0, 2);
        return kept + "***" + email.substring(at);
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /** 密文类字段只保留前后各 4 字节及长度，用于设备报文记录。 */
    public static String maskCipherHex(String hex) {
        if (hex == null || hex.isEmpty()) {
            return hex;
        }
        int bytes = hex.length() / 2;
        if (bytes <= 8) {
            return hex + "(" + bytes + "B)";
        }
        return hex.substring(0, 8) + "..." + hex.substring(hex.length() - 8) + "(" + bytes + "B)";
    }
}

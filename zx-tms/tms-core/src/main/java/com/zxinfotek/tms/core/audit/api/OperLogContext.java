package com.zxinfotek.tms.core.audit.api;

import com.zxinfotek.tms.common.enums.OperResult;

/**
 * 操作日志补充上下文：业务方法在执行过程中补充业务对象与变更摘要，由切面在方法结束时取出并写入。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
public final class OperLogContext {

    private static final ThreadLocal<Holder> HOLDER = new ThreadLocal<>();

    private OperLogContext() {
    }

    public static void object(String objectType, String objectId, String objectName) {
        Holder holder = current();
        holder.objectType = objectType;
        holder.objectId = objectId;
        holder.objectName = objectName;
    }

    public static void changeSummary(String changeSummary) {
        current().changeSummary = changeSummary;
    }

    public static void counts(Integer total, Integer success) {
        Holder holder = current();
        holder.totalCount = total;
        holder.successCount = success;
        if (total != null && success != null && success < total) {
            holder.result = OperResult.PARTIAL;
        }
    }

    public static Holder drain() {
        Holder holder = HOLDER.get();
        HOLDER.remove();
        return holder;
    }

    private static Holder current() {
        Holder holder = HOLDER.get();
        if (holder == null) {
            holder = new Holder();
            HOLDER.set(holder);
        }
        return holder;
    }

    public static class Holder {
        private String objectType;
        private String objectId;
        private String objectName;
        private String changeSummary;
        private Integer totalCount;
        private Integer successCount;
        private OperResult result;

        public String getObjectType() {
            return objectType;
        }

        public String getObjectId() {
            return objectId;
        }

        public String getObjectName() {
            return objectName;
        }

        public String getChangeSummary() {
            return changeSummary;
        }

        public Integer getTotalCount() {
            return totalCount;
        }

        public Integer getSuccessCount() {
            return successCount;
        }

        public OperResult getResult() {
            return result;
        }
    }
}

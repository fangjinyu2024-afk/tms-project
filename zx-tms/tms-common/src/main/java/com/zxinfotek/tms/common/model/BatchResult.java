package com.zxinfotek.tms.common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 批量业务的部分成功响应体，批量接口一律返回成功状态并在本结构中区分成败（详细设计 7.10）。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
public class BatchResult implements Serializable {

    private String batchNo;
    private int total;
    private int successCount;
    private int failCount;
    private List<Failure> failures = new ArrayList<>();

    public void addFailure(String key, String reason) {
        failures.add(new Failure(key, reason));
        failCount = failures.size();
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    public List<Failure> getFailures() {
        return failures;
    }

    public void setFailures(List<Failure> failures) {
        this.failures = failures;
    }

    public static class Failure implements Serializable {
        private String key;
        private String reason;

        public Failure() {
        }

        public Failure(String key, String reason) {
            this.key = key;
            this.reason = reason;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}

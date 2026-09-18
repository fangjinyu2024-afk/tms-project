package com.zxinfotek.tms.common.model;

import java.io.Serializable;

public class PageQuery implements Serializable {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 200;

    private Integer pageNum = 1;
    private Integer pageSize = DEFAULT_PAGE_SIZE;

    public long resolvePageNum() {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    public long resolvePageSize() {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}

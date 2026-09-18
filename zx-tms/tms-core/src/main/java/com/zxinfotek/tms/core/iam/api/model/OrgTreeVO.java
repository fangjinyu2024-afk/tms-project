package com.zxinfotek.tms.core.iam.api.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OrgTreeVO {

    private Long id;
    private Long parentId;
    private String name;
    private String orgType;
    private String status;
    private String orgPath;
    /** 本机构及下级机构数 */
    private Integer subCount;
    private List<OrgTreeVO> children = new ArrayList<>();
}

package com.zxinfotek.tms.core.iam.api.model;

import com.zxinfotek.tms.common.enums.EnableStatus;
import com.zxinfotek.tms.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrgQuery extends PageQuery {

    private String keyword;
    private EnableStatus status;
    /** 按该机构及下级筛选 */
    private Long orgId;
}

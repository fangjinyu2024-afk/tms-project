package com.zxinfotek.tms.core.iam.api.model;

import com.zxinfotek.tms.common.enums.EnableStatus;
import com.zxinfotek.tms.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MemberQuery extends PageQuery {

    private String keyword;
    private Long orgId;
    private EnableStatus status;
}

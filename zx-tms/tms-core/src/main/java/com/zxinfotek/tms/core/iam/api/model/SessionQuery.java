package com.zxinfotek.tms.core.iam.api.model;

import com.zxinfotek.tms.common.enums.SessionEntry;
import com.zxinfotek.tms.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SessionQuery extends PageQuery {

    private String keyword;
    private SessionEntry entry;
    private Long orgId;
}

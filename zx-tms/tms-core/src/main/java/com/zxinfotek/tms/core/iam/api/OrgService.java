package com.zxinfotek.tms.core.iam.api;

import com.zxinfotek.tms.common.model.PageResult;
import com.zxinfotek.tms.core.iam.api.model.OrgCreateResultVO;
import com.zxinfotek.tms.core.iam.api.model.OrgQuery;
import com.zxinfotek.tms.core.iam.api.model.OrgSaveRequest;
import com.zxinfotek.tms.core.iam.api.model.OrgTreeVO;
import com.zxinfotek.tms.core.iam.api.model.OrgVO;
import com.zxinfotek.tms.core.iam.api.model.StatusRequest;

import java.util.List;

public interface OrgService {

    List<OrgTreeVO> tree();

    List<OrgTreeVO> selector();

    PageResult<OrgVO> page(OrgQuery query);

    OrgVO detail(Long id);

    OrgCreateResultVO create(OrgSaveRequest request);

    void update(Long id, OrgSaveRequest request);

    void changeStatus(Long id, StatusRequest request);

    void delete(Long id);

    String export(OrgQuery query);

    String orgName(Long orgId);

    String orgPath(Long orgId);
}

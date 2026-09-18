package com.zxinfotek.tms.core.iam.api;

import com.zxinfotek.tms.common.model.PageResult;
import com.zxinfotek.tms.core.iam.api.model.AssignRoleRequest;
import com.zxinfotek.tms.core.iam.api.model.MemberCreateResultVO;
import com.zxinfotek.tms.core.iam.api.model.MemberQuery;
import com.zxinfotek.tms.core.iam.api.model.MemberSaveRequest;
import com.zxinfotek.tms.core.iam.api.model.MemberVO;
import com.zxinfotek.tms.core.iam.api.model.StatusRequest;

public interface MemberService {

    PageResult<MemberVO> page(MemberQuery query);

    MemberVO detail(Long id);

    MemberCreateResultVO create(MemberSaveRequest request);

    void update(Long id, MemberSaveRequest request);

    void changeStatus(Long id, StatusRequest request);

    void delete(Long id);

    MemberCreateResultVO resetPassword(Long id);

    void assignRoles(Long id, AssignRoleRequest request);

    String export(MemberQuery query);
}

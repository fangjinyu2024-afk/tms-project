package com.zxinfotek.tms.core.audit.api;

import com.zxinfotek.tms.common.model.PageResult;
import com.zxinfotek.tms.core.audit.api.model.LoginLogQuery;
import com.zxinfotek.tms.core.audit.api.model.LoginLogRecord;
import com.zxinfotek.tms.core.audit.api.model.LoginLogVO;

public interface LoginLogService {

    void record(LoginLogRecord record);

    PageResult<LoginLogVO> page(LoginLogQuery query);

    String export(LoginLogQuery query);
}

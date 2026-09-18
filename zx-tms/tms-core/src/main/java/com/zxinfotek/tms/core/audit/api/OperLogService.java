package com.zxinfotek.tms.core.audit.api;

import com.zxinfotek.tms.common.model.PageResult;
import com.zxinfotek.tms.core.audit.api.model.OperLogQuery;
import com.zxinfotek.tms.core.audit.api.model.OperLogRecord;
import com.zxinfotek.tms.core.audit.api.model.OperLogVO;

public interface OperLogService {

    /** 异步写入业务操作日志，写入失败不影响主业务事务。 */
    void record(OperLogRecord record);

    PageResult<OperLogVO> page(OperLogQuery query);

    OperLogVO detail(Long id);

    String export(OperLogQuery query);
}

package com.zxinfotek.tms.admin.controller;

import com.zxinfotek.tms.admin.aop.OperationLog;
import com.zxinfotek.tms.admin.aop.RequiresPerm;
import com.zxinfotek.tms.common.enums.LogModule;
import com.zxinfotek.tms.common.enums.OperAction;
import com.zxinfotek.tms.common.model.PageResult;
import com.zxinfotek.tms.common.model.Result;
import com.zxinfotek.tms.core.audit.api.OperLogService;
import com.zxinfotek.tms.core.audit.api.model.OperLogQuery;
import com.zxinfotek.tms.core.audit.api.model.OperLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "操作日志")
@RestController
@RequestMapping("/api/oper-logs")
public class OperLogController {

    private final OperLogService operLogService;

    public OperLogController(OperLogService operLogService) {
        this.operLogService = operLogService;
    }

    @Operation(summary = "操作日志分页查询")
    @GetMapping
    @RequiresPerm("logs:view")
    public Result<PageResult<OperLogVO>> page(OperLogQuery query) {
        return Result.ok(operLogService.page(query));
    }

    @Operation(summary = "导出操作日志")
    @GetMapping("/export")
    @RequiresPerm("logs:export")
    @OperationLog(module = LogModule.AUTH, action = OperAction.EXPORT, objectType = "OPER_LOG")
    public Result<String> export(OperLogQuery query) {
        return Result.ok(operLogService.export(query));
    }

    @Operation(summary = "操作日志详情含变更摘要")
    @GetMapping("/{id}")
    @RequiresPerm("logs:view")
    public Result<OperLogVO> detail(@PathVariable Long id) {
        return Result.ok(operLogService.detail(id));
    }
}

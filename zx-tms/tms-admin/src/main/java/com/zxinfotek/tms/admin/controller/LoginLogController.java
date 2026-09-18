package com.zxinfotek.tms.admin.controller;

import com.zxinfotek.tms.admin.aop.OperationLog;
import com.zxinfotek.tms.admin.aop.RequiresPerm;
import com.zxinfotek.tms.common.enums.LogModule;
import com.zxinfotek.tms.common.enums.OperAction;
import com.zxinfotek.tms.common.model.PageResult;
import com.zxinfotek.tms.common.model.Result;
import com.zxinfotek.tms.core.audit.api.LoginLogService;
import com.zxinfotek.tms.core.audit.api.model.LoginLogQuery;
import com.zxinfotek.tms.core.audit.api.model.LoginLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "登录日志")
@RestController
@RequestMapping("/api/login-logs")
public class LoginLogController {

    private final LoginLogService loginLogService;

    public LoginLogController(LoginLogService loginLogService) {
        this.loginLogService = loginLogService;
    }

    @Operation(summary = "登录日志分页查询")
    @GetMapping
    @RequiresPerm("logins:view")
    public Result<PageResult<LoginLogVO>> page(LoginLogQuery query) {
        return Result.ok(loginLogService.page(query));
    }

    @Operation(summary = "导出登录日志")
    @GetMapping("/export")
    @RequiresPerm("logins:export")
    @OperationLog(module = LogModule.AUTH, action = OperAction.EXPORT, objectType = "LOGIN_LOG")
    public Result<String> export(LoginLogQuery query) {
        return Result.ok(loginLogService.export(query));
    }
}

package com.zxinfotek.tms.admin.controller;

import com.zxinfotek.tms.admin.aop.OperationLog;
import com.zxinfotek.tms.admin.aop.RequiresPerm;
import com.zxinfotek.tms.common.enums.LogModule;
import com.zxinfotek.tms.common.enums.OperAction;
import com.zxinfotek.tms.common.model.PageResult;
import com.zxinfotek.tms.common.model.Result;
import com.zxinfotek.tms.core.iam.api.LoginSessionService;
import com.zxinfotek.tms.core.iam.api.model.ForceLogoutRequest;
import com.zxinfotek.tms.core.iam.api.model.SessionQuery;
import com.zxinfotek.tms.core.iam.api.model.SessionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "在线会话")
@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final LoginSessionService loginSessionService;

    public SessionController(LoginSessionService loginSessionService) {
        this.loginSessionService = loginSessionService;
    }

    @Operation(summary = "在线会话分页查询")
    @GetMapping
    @RequiresPerm("sessions:view")
    public Result<PageResult<SessionVO>> page(SessionQuery query) {
        return Result.ok(loginSessionService.page(query));
    }

    @Operation(summary = "导出在线会话")
    @GetMapping("/export")
    @RequiresPerm("sessions:view")
    @OperationLog(module = LogModule.SESSION, action = OperAction.EXPORT, objectType = "SESSION")
    public Result<String> export(SessionQuery query) {
        return Result.ok(loginSessionService.export(query));
    }

    @Operation(summary = "强制下线单条会话")
    @PostMapping("/{id}/force-logout")
    @RequiresPerm("sessions:force")
    @OperationLog(module = LogModule.SESSION, action = OperAction.FORCE_LOGOUT, objectType = "SESSION",
            summaryFields = {"reason"})
    public Result<Void> forceLogout(@PathVariable Long id, @Valid @RequestBody ForceLogoutRequest request) {
        loginSessionService.forceLogout(id, request);
        return Result.ok();
    }
}

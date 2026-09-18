package com.zxinfotek.tms.admin.controller;

import com.zxinfotek.tms.admin.aop.OperationLog;
import com.zxinfotek.tms.common.enums.LogModule;
import com.zxinfotek.tms.common.enums.OperAction;
import com.zxinfotek.tms.common.model.Result;
import com.zxinfotek.tms.core.iam.api.AuthService;
import com.zxinfotek.tms.core.iam.api.model.ChangePasswordRequest;
import com.zxinfotek.tms.core.iam.api.model.EmailVerifyRequest;
import com.zxinfotek.tms.core.iam.api.model.ForgotPasswordRequest;
import com.zxinfotek.tms.core.iam.api.model.LoginRequest;
import com.zxinfotek.tms.core.iam.api.model.LoginVO;
import com.zxinfotek.tms.core.iam.api.model.ProfileVO;
import com.zxinfotek.tms.core.iam.api.model.ResetPasswordRequest;
import com.zxinfotek.tms.core.iam.api.model.UpdateProfileRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "认证与账号")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "账号密码登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.ok();
    }

    @Operation(summary = "当前登录人信息与有效权限")
    @GetMapping("/profile")
    public Result<ProfileVO> profile() {
        return Result.ok(authService.currentProfile());
    }

    @Operation(summary = "维护个人资料")
    @PutMapping("/profile")
    @OperationLog(module = LogModule.AUTH, action = OperAction.UPDATE, objectType = "MEMBER",
            summaryFields = {"nickname", "email", "phone"})
    public Result<Void> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        authService.updateProfile(request);
        return Result.ok();
    }

    @Operation(summary = "修改本人密码")
    @PostMapping("/password")
    @OperationLog(module = LogModule.AUTH, action = OperAction.RESET_PASSWORD, objectType = "MEMBER")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return Result.ok();
    }

    @Operation(summary = "发起找回密码")
    @PostMapping("/password/forgot")
    public Result<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return Result.ok();
    }

    @Operation(summary = "凭邮件令牌重置密码")
    @PostMapping("/password/reset")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPasswordByToken(request);
        return Result.ok();
    }

    @Operation(summary = "发起或确认邮箱验证")
    @PostMapping("/email/verify")
    public Result<Void> emailVerify(@RequestBody EmailVerifyRequest request) {
        authService.emailVerify(request);
        return Result.ok();
    }
}

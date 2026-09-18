package com.zxinfotek.tms.admin.controller;

import com.zxinfotek.tms.admin.aop.OperationLog;
import com.zxinfotek.tms.admin.aop.RequiresPerm;
import com.zxinfotek.tms.common.enums.LogModule;
import com.zxinfotek.tms.common.enums.OperAction;
import com.zxinfotek.tms.common.model.PageResult;
import com.zxinfotek.tms.common.model.Result;
import com.zxinfotek.tms.core.iam.api.MemberService;
import com.zxinfotek.tms.core.iam.api.model.AssignRoleRequest;
import com.zxinfotek.tms.core.iam.api.model.MemberCreateResultVO;
import com.zxinfotek.tms.core.iam.api.model.MemberQuery;
import com.zxinfotek.tms.core.iam.api.model.MemberSaveRequest;
import com.zxinfotek.tms.core.iam.api.model.MemberVO;
import com.zxinfotek.tms.core.iam.api.model.StatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "成员管理")
@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @Operation(summary = "成员分页查询")
    @GetMapping
    @RequiresPerm("members:view")
    public Result<PageResult<MemberVO>> page(MemberQuery query) {
        return Result.ok(memberService.page(query));
    }

    @Operation(summary = "导出成员")
    @GetMapping("/export")
    @RequiresPerm("members:export")
    @OperationLog(module = LogModule.MEMBER, action = OperAction.EXPORT)
    public Result<String> export(MemberQuery query) {
        return Result.ok(memberService.export(query));
    }

    @Operation(summary = "成员详情含角色")
    @GetMapping("/{id}")
    @RequiresPerm("members:view")
    public Result<MemberVO> detail(@PathVariable Long id) {
        return Result.ok(memberService.detail(id));
    }

    @Operation(summary = "新增成员，返回系统生成的初始密码")
    @PostMapping
    @RequiresPerm("members:create")
    @OperationLog(module = LogModule.MEMBER, action = OperAction.CREATE, objectType = "MEMBER",
            summaryFields = {"account", "nickname", "orgId", "roleIds"})
    public Result<MemberCreateResultVO> create(@Valid @RequestBody MemberSaveRequest request) {
        return Result.ok(memberService.create(request));
    }

    @Operation(summary = "编辑成员")
    @PutMapping("/{id}")
    @RequiresPerm("members:edit")
    @OperationLog(module = LogModule.MEMBER, action = OperAction.UPDATE, objectType = "MEMBER",
            summaryFields = {"nickname", "email", "phone", "roleIds"})
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody MemberSaveRequest request) {
        memberService.update(id, request);
        return Result.ok();
    }

    @Operation(summary = "启用或停用成员")
    @PostMapping("/{id}/status")
    @RequiresPerm("members:toggle")
    @OperationLog(module = LogModule.MEMBER, action = OperAction.TOGGLE, objectType = "MEMBER",
            summaryFields = {"status"})
    public Result<Void> changeStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        memberService.changeStatus(id, request);
        return Result.ok();
    }

    @Operation(summary = "删除成员")
    @DeleteMapping("/{id}")
    @RequiresPerm("members:delete")
    @OperationLog(module = LogModule.MEMBER, action = OperAction.DELETE, objectType = "MEMBER")
    public Result<Void> delete(@PathVariable Long id) {
        memberService.delete(id);
        return Result.ok();
    }

    @Operation(summary = "重置密码，返回新的初始密码")
    @PostMapping("/{id}/password/reset")
    @RequiresPerm("members:reset")
    @OperationLog(module = LogModule.MEMBER, action = OperAction.RESET_PASSWORD, objectType = "MEMBER")
    public Result<MemberCreateResultVO> resetPassword(@PathVariable Long id) {
        return Result.ok(memberService.resetPassword(id));
    }

    @Operation(summary = "分配角色")
    @PutMapping("/{id}/roles")
    @RequiresPerm("members:assign")
    @OperationLog(module = LogModule.MEMBER, action = OperAction.ASSIGN_ROLE, objectType = "MEMBER",
            summaryFields = {"roleIds"})
    public Result<Void> assignRoles(@PathVariable Long id, @Valid @RequestBody AssignRoleRequest request) {
        memberService.assignRoles(id, request);
        return Result.ok();
    }
}

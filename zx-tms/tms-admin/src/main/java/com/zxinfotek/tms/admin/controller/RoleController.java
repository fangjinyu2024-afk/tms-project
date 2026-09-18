package com.zxinfotek.tms.admin.controller;

import com.zxinfotek.tms.admin.aop.OperationLog;
import com.zxinfotek.tms.admin.aop.RequiresPerm;
import com.zxinfotek.tms.common.enums.LogModule;
import com.zxinfotek.tms.common.enums.OperAction;
import com.zxinfotek.tms.common.model.PageResult;
import com.zxinfotek.tms.common.model.Result;
import com.zxinfotek.tms.core.iam.api.RoleService;
import com.zxinfotek.tms.core.iam.api.model.RoleOptionVO;
import com.zxinfotek.tms.core.iam.api.model.RoleQuery;
import com.zxinfotek.tms.core.iam.api.model.RoleSaveRequest;
import com.zxinfotek.tms.core.iam.api.model.RoleVO;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "角色与权限")
@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @Operation(summary = "角色分页查询")
    @GetMapping
    @RequiresPerm("roles:view")
    public Result<PageResult<RoleVO>> page(RoleQuery query) {
        return Result.ok(roleService.page(query));
    }

    @Operation(summary = "成员分配角色时的候选角色")
    @GetMapping("/options")
    @RequiresPerm("members:assign")
    public Result<List<RoleOptionVO>> options(@RequestParam Long orgId) {
        return Result.ok(roleService.assignableOptions(orgId));
    }

    @Operation(summary = "导出角色")
    @GetMapping("/export")
    @RequiresPerm("roles:export")
    @OperationLog(module = LogModule.ROLE, action = OperAction.EXPORT)
    public Result<String> export(RoleQuery query) {
        return Result.ok(roleService.export(query));
    }

    @Operation(summary = "角色详情含权限码")
    @GetMapping("/{id}")
    @RequiresPerm("roles:view")
    public Result<RoleVO> detail(@PathVariable Long id) {
        return Result.ok(roleService.detail(id));
    }

    @Operation(summary = "新增角色")
    @PostMapping
    @RequiresPerm("roles:create")
    @OperationLog(module = LogModule.ROLE, action = OperAction.CREATE, objectType = "ROLE",
            summaryFields = {"name", "dataScope", "permCodes"})
    public Result<Long> create(@Valid @RequestBody RoleSaveRequest request) {
        return Result.ok(roleService.create(request));
    }

    @Operation(summary = "编辑角色与权限")
    @PutMapping("/{id}")
    @RequiresPerm("roles:edit")
    @OperationLog(module = LogModule.ROLE, action = OperAction.UPDATE, objectType = "ROLE",
            summaryFields = {"name", "dataScope", "permCodes"})
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody RoleSaveRequest request) {
        roleService.update(id, request);
        return Result.ok();
    }

    @Operation(summary = "复制角色")
    @PostMapping("/{id}/copy")
    @RequiresPerm("roles:copy")
    @OperationLog(module = LogModule.ROLE, action = OperAction.CREATE, objectType = "ROLE")
    public Result<Long> copy(@PathVariable Long id) {
        return Result.ok(roleService.copy(id));
    }

    @Operation(summary = "启用或停用角色")
    @PostMapping("/{id}/status")
    @RequiresPerm("roles:toggle")
    @OperationLog(module = LogModule.ROLE, action = OperAction.TOGGLE, objectType = "ROLE",
            summaryFields = {"status"})
    public Result<Void> changeStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        roleService.changeStatus(id, request);
        return Result.ok();
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    @RequiresPerm("roles:delete")
    @OperationLog(module = LogModule.ROLE, action = OperAction.DELETE, objectType = "ROLE")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.ok();
    }
}

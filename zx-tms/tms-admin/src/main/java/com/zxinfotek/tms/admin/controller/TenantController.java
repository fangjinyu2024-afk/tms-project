package com.zxinfotek.tms.admin.controller;

import com.zxinfotek.tms.admin.aop.OperationLog;
import com.zxinfotek.tms.admin.aop.RequiresPerm;
import com.zxinfotek.tms.common.enums.LogModule;
import com.zxinfotek.tms.common.enums.OperAction;
import com.zxinfotek.tms.common.model.PageResult;
import com.zxinfotek.tms.common.model.Result;
import com.zxinfotek.tms.core.iam.api.TenantService;
import com.zxinfotek.tms.core.iam.api.model.StatusRequest;
import com.zxinfotek.tms.core.iam.api.model.TenantCreateResultVO;
import com.zxinfotek.tms.core.iam.api.model.TenantFeatureSaveRequest;
import com.zxinfotek.tms.core.iam.api.model.TenantFeatureVO;
import com.zxinfotek.tms.core.iam.api.model.TenantQuery;
import com.zxinfotek.tms.core.iam.api.model.TenantSaveRequest;
import com.zxinfotek.tms.core.iam.api.model.TenantVO;
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

@Tag(name = "客户与功能授权")
@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @Operation(summary = "客户分页查询")
    @GetMapping
    @RequiresPerm("customers:view")
    public Result<PageResult<TenantVO>> page(TenantQuery query) {
        return Result.ok(tenantService.page(query));
    }

    @Operation(summary = "导出客户")
    @GetMapping("/export")
    @RequiresPerm("customers:export")
    @OperationLog(module = LogModule.TENANT, action = OperAction.EXPORT)
    public Result<String> export(TenantQuery query) {
        return Result.ok(tenantService.export(query));
    }

    @Operation(summary = "客户详情")
    @GetMapping("/{id}")
    @RequiresPerm("customers:view")
    public Result<TenantVO> detail(@PathVariable Long id) {
        return Result.ok(tenantService.detail(id));
    }

    @Operation(summary = "新增客户及其根机构与管理员")
    @PostMapping
    @RequiresPerm("customers:create")
    @OperationLog(module = LogModule.TENANT, action = OperAction.CREATE, objectType = "TENANT",
            summaryFields = {"name", "contactName", "modelIds", "menuKeys"})
    public Result<TenantCreateResultVO> create(@Valid @RequestBody TenantSaveRequest request) {
        return Result.ok(tenantService.create(request));
    }

    @Operation(summary = "编辑客户")
    @PutMapping("/{id}")
    @RequiresPerm("customers:edit")
    @OperationLog(module = LogModule.TENANT, action = OperAction.UPDATE, objectType = "TENANT",
            summaryFields = {"name", "contactName", "contactPhone", "modelIds"})
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody TenantSaveRequest request) {
        tenantService.update(id, request);
        return Result.ok();
    }

    @Operation(summary = "启用或停用客户")
    @PostMapping("/{id}/status")
    @RequiresPerm("customers:toggle")
    @OperationLog(module = LogModule.TENANT, action = OperAction.TOGGLE, objectType = "TENANT",
            summaryFields = {"status"})
    public Result<Void> changeStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        tenantService.changeStatus(id, request);
        return Result.ok();
    }

    @Operation(summary = "删除客户")
    @DeleteMapping("/{id}")
    @RequiresPerm("customers:delete")
    @OperationLog(module = LogModule.TENANT, action = OperAction.DELETE, objectType = "TENANT")
    public Result<Void> delete(@PathVariable Long id) {
        tenantService.delete(id);
        return Result.ok();
    }

    @Operation(summary = "客户功能授权可选菜单")
    @GetMapping("/feature-options")
    @RequiresPerm("customers:authorize")
    public Result<java.util.List<TenantFeatureVO.MenuOptionVO>> featureOptions() {
        return Result.ok(tenantService.featureOptions());
    }

    @Operation(summary = "查询客户功能授权")
    @GetMapping("/{id}/features")
    @RequiresPerm("customers:authorize")
    public Result<TenantFeatureVO> features(@PathVariable Long id) {
        return Result.ok(tenantService.features(id));
    }

    @Operation(summary = "保存客户功能授权")
    @PutMapping("/{id}/features")
    @RequiresPerm("customers:authorize")
    @OperationLog(module = LogModule.TENANT, action = OperAction.AUTHORIZE, objectType = "TENANT",
            summaryFields = {"menuKeys"})
    public Result<TenantFeatureVO> saveFeatures(@PathVariable Long id,
                                                @Valid @RequestBody TenantFeatureSaveRequest request) {
        return Result.ok(tenantService.saveFeatures(id, request));
    }
}

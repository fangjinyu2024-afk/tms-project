package com.zxinfotek.tms.admin.controller;

import com.zxinfotek.tms.admin.aop.OperationLog;
import com.zxinfotek.tms.admin.aop.RequiresPerm;
import com.zxinfotek.tms.common.enums.LogModule;
import com.zxinfotek.tms.common.enums.OperAction;
import com.zxinfotek.tms.common.model.PageResult;
import com.zxinfotek.tms.common.model.Result;
import com.zxinfotek.tms.core.iam.api.OrgService;
import com.zxinfotek.tms.core.iam.api.model.OrgCreateResultVO;
import com.zxinfotek.tms.core.iam.api.model.OrgQuery;
import com.zxinfotek.tms.core.iam.api.model.OrgSaveRequest;
import com.zxinfotek.tms.core.iam.api.model.OrgTreeVO;
import com.zxinfotek.tms.core.iam.api.model.OrgVO;
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

import java.util.List;

@Tag(name = "机构管理")
@RestController
@RequestMapping("/api/orgs")
public class OrgController {

    private final OrgService orgService;

    public OrgController(OrgService orgService) {
        this.orgService = orgService;
    }

    @Operation(summary = "权限范围内的机构树")
    @GetMapping("/tree")
    @RequiresPerm("orgs:view")
    public Result<List<OrgTreeVO>> tree() {
        return Result.ok(orgService.tree());
    }

    @Operation(summary = "可选上级机构树")
    @GetMapping("/selector")
    @RequiresPerm(anyOf = {"orgs:create", "orgs:edit"})
    public Result<List<OrgTreeVO>> selector() {
        return Result.ok(orgService.selector());
    }

    @Operation(summary = "机构分页查询")
    @GetMapping
    @RequiresPerm("orgs:view")
    public Result<PageResult<OrgVO>> page(OrgQuery query) {
        return Result.ok(orgService.page(query));
    }

    @Operation(summary = "导出机构")
    @GetMapping("/export")
    @RequiresPerm("orgs:export")
    @OperationLog(module = LogModule.ORG, action = OperAction.EXPORT)
    public Result<String> export(OrgQuery query) {
        return Result.ok(orgService.export(query));
    }

    @Operation(summary = "机构详情")
    @GetMapping("/{id}")
    @RequiresPerm("orgs:view")
    public Result<OrgVO> detail(@PathVariable Long id) {
        return Result.ok(orgService.detail(id));
    }

    @Operation(summary = "新增机构及其管理员")
    @PostMapping
    @RequiresPerm("orgs:create")
    @OperationLog(module = LogModule.ORG, action = OperAction.CREATE, objectType = "ORG",
            summaryFields = {"name", "parentId", "contactName"})
    public Result<OrgCreateResultVO> create(@Valid @RequestBody OrgSaveRequest request) {
        return Result.ok(orgService.create(request));
    }

    @Operation(summary = "编辑机构")
    @PutMapping("/{id}")
    @RequiresPerm("orgs:edit")
    @OperationLog(module = LogModule.ORG, action = OperAction.UPDATE, objectType = "ORG",
            summaryFields = {"name", "contactName", "contactPhone"})
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody OrgSaveRequest request) {
        orgService.update(id, request);
        return Result.ok();
    }

    @Operation(summary = "启用或停用机构")
    @PostMapping("/{id}/status")
    @RequiresPerm("orgs:toggle")
    @OperationLog(module = LogModule.ORG, action = OperAction.TOGGLE, objectType = "ORG",
            summaryFields = {"status"})
    public Result<Void> changeStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        orgService.changeStatus(id, request);
        return Result.ok();
    }

    @Operation(summary = "删除机构")
    @DeleteMapping("/{id}")
    @RequiresPerm("orgs:delete")
    @OperationLog(module = LogModule.ORG, action = OperAction.DELETE, objectType = "ORG")
    public Result<Void> delete(@PathVariable Long id) {
        orgService.delete(id);
        return Result.ok();
    }
}

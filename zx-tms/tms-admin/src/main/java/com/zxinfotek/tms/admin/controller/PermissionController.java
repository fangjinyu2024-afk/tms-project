package com.zxinfotek.tms.admin.controller;

import com.zxinfotek.tms.admin.aop.RequiresPerm;
import com.zxinfotek.tms.common.model.Result;
import com.zxinfotek.tms.core.iam.api.PermissionService;
import com.zxinfotek.tms.core.iam.api.model.PermissionCatalogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "权限目录")
@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Operation(summary = "权限目录，按菜单分组返回可配置项")
    @GetMapping("/catalog")
    @RequiresPerm("roles:view")
    public Result<List<PermissionCatalogVO>> catalog(@RequestParam(required = false) Long orgId) {
        return Result.ok(permissionService.catalog(orgId));
    }

    @Operation(summary = "操作者对指定机构可授予的权限码")
    @GetMapping("/grantable")
    @RequiresPerm(anyOf = {"roles:create", "roles:edit"})
    public Result<List<String>> grantable(@RequestParam(required = false) Long orgId) {
        return Result.ok(new ArrayList<>(permissionService.grantablePermCodes(orgId)));
    }
}

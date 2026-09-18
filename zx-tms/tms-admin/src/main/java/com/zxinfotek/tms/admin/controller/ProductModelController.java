package com.zxinfotek.tms.admin.controller;

import com.zxinfotek.tms.admin.aop.RequiresPerm;
import com.zxinfotek.tms.common.model.Result;
import com.zxinfotek.tms.core.product.api.ProductModelService;
import com.zxinfotek.tms.core.product.api.model.ModelOptionVO;
import com.zxinfotek.tms.infra.context.RequestContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "型号下拉")
@RestController
@RequestMapping("/api/product-models")
public class ProductModelController {

    private final ProductModelService productModelService;

    public ProductModelController(ProductModelService productModelService) {
        this.productModelService = productModelService;
    }

    @Operation(summary = "型号下拉，按客户授权过滤")
    @GetMapping("/options")
    @RequiresPerm(anyOf = {"products:view", "customers:view", "devices:view", "groups:view",
            "packages:view", "ota:view", "rki:view", "mode:view"})
    public Result<List<ModelOptionVO>> options(@RequestParam(required = false) Long tenantId) {
        Long target = RequestContextHolder.get().isPlatform() && tenantId != null
                ? tenantId : RequestContextHolder.get().getTenantId();
        return Result.ok(productModelService.options(target));
    }
}

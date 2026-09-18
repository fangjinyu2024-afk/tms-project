package com.zxinfotek.tms.admin.controller;

import com.zxinfotek.tms.admin.aop.OperationLog;
import com.zxinfotek.tms.admin.aop.RequiresPerm;
import com.zxinfotek.tms.common.enums.LogModule;
import com.zxinfotek.tms.common.enums.OperAction;
import com.zxinfotek.tms.common.model.PageResult;
import com.zxinfotek.tms.common.model.Result;
import com.zxinfotek.tms.core.product.api.ProductService;
import com.zxinfotek.tms.core.product.api.model.ProductQuery;
import com.zxinfotek.tms.core.product.api.model.ProductSaveRequest;
import com.zxinfotek.tms.core.product.api.model.ProductVO;
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

@Tag(name = "产品与型号")
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "产品分页查询")
    @GetMapping
    @RequiresPerm("products:view")
    public Result<PageResult<ProductVO>> page(ProductQuery query) {
        return Result.ok(productService.page(query));
    }

    @Operation(summary = "导出产品")
    @GetMapping("/export")
    @RequiresPerm("products:export")
    @OperationLog(module = LogModule.PRODUCT, action = OperAction.EXPORT)
    public Result<String> export(ProductQuery query) {
        return Result.ok(productService.export(query));
    }

    @Operation(summary = "产品详情含型号")
    @GetMapping("/{id}")
    @RequiresPerm("products:view")
    public Result<ProductVO> detail(@PathVariable Long id) {
        return Result.ok(productService.detail(id));
    }

    @Operation(summary = "新增产品与型号")
    @PostMapping
    @RequiresPerm("products:create")
    @OperationLog(module = LogModule.PRODUCT, action = OperAction.CREATE, objectType = "PRODUCT",
            summaryFields = {"name", "category"})
    public Result<Long> create(@Valid @RequestBody ProductSaveRequest request) {
        return Result.ok(productService.create(request));
    }

    @Operation(summary = "编辑产品与型号")
    @PutMapping("/{id}")
    @RequiresPerm("products:edit")
    @OperationLog(module = LogModule.PRODUCT, action = OperAction.UPDATE, objectType = "PRODUCT",
            summaryFields = {"name", "category"})
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ProductSaveRequest request) {
        productService.update(id, request);
        return Result.ok();
    }

    @Operation(summary = "删除产品")
    @DeleteMapping("/{id}")
    @RequiresPerm("products:delete")
    @OperationLog(module = LogModule.PRODUCT, action = OperAction.DELETE, objectType = "PRODUCT")
    public Result<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return Result.ok();
    }
}

package com.zxinfotek.tms.core.product.service.impl;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zxinfotek.tms.common.exception.BizException;
import com.zxinfotek.tms.common.exception.NotFoundException;
import com.zxinfotek.tms.common.model.PageResult;
import com.zxinfotek.tms.core.product.ProductErrorCode;
import com.zxinfotek.tms.core.product.api.ModelReferenceProvider;
import com.zxinfotek.tms.core.product.api.ProductService;
import com.zxinfotek.tms.core.product.api.model.ProductModelVO;
import com.zxinfotek.tms.core.product.api.model.ProductQuery;
import com.zxinfotek.tms.core.product.api.model.ProductSaveRequest;
import com.zxinfotek.tms.core.product.api.model.ProductVO;
import com.zxinfotek.tms.core.product.entity.ProductEntity;
import com.zxinfotek.tms.core.product.entity.ProductModelEntity;
import com.zxinfotek.tms.core.product.entity.TenantModelEntity;
import com.zxinfotek.tms.core.product.mapper.ProductMapper;
import com.zxinfotek.tms.core.product.mapper.ProductModelMapper;
import com.zxinfotek.tms.core.product.mapper.TenantModelMapper;
import com.zxinfotek.tms.infra.context.RequestContextHolder;
import com.zxinfotek.tms.infra.excel.ExcelExportService;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 产品与型号主数据维护，由平台维护，客户账户不提供该菜单（详细设计 3.8）。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final ProductModelMapper productModelMapper;
    private final TenantModelMapper tenantModelMapper;
    private final ExcelExportService excelExportService;
    private final List<ModelReferenceProvider> referenceProviders;

    public ProductServiceImpl(ProductMapper productMapper,
                              ProductModelMapper productModelMapper,
                              TenantModelMapper tenantModelMapper,
                              ExcelExportService excelExportService,
                              List<ModelReferenceProvider> referenceProviders) {
        this.productMapper = productMapper;
        this.productModelMapper = productModelMapper;
        this.tenantModelMapper = tenantModelMapper;
        this.excelExportService = excelExportService;
        this.referenceProviders = referenceProviders;
    }

    @Override
    public PageResult<ProductVO> page(ProductQuery query) {
        Page<ProductEntity> page = new Page<>(query.resolvePageNum(), query.resolvePageSize());
        Page<ProductEntity> result = productMapper.selectPage(page, buildWrapper(query));
        List<ProductVO> rows = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        fillModels(rows);
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), rows);
    }

    @Override
    public ProductVO detail(Long id) {
        ProductEntity product = requireProduct(id);
        ProductVO vo = toVO(product);
        fillModels(List.of(vo));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ProductSaveRequest request) {
        assertNameUnique(request.getName(), null);
        ProductEntity product = new ProductEntity();
        product.setCategory(request.getCategory());
        product.setName(request.getName());
        product.setImagePath(request.getImagePath());
        product.setDescription(request.getDescription());
        product.setDeleted(0);
        productMapper.insert(product);
        saveModels(product.getId(), request.getModels());
        return product.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ProductSaveRequest request) {
        ProductEntity product = requireProduct(id);
        assertNameUnique(request.getName(), id);
        product.setCategory(request.getCategory());
        product.setName(request.getName());
        product.setImagePath(request.getImagePath());
        product.setDescription(request.getDescription());
        productMapper.updateById(product);
        saveModels(id, request.getModels());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        requireProduct(id);
        List<ProductModelEntity> models = modelsOf(List.of(id));
        for (ProductModelEntity model : models) {
            assertModelRemovable(model);
        }
        models.forEach(model -> productModelMapper.deleteById(model.getId()));
        productMapper.deleteById(id);
    }

    @Override
    public String export(ProductQuery query) {
        List<ProductEntity> products = productMapper
                .selectPage(new Page<>(1, 10000), buildWrapper(query)).getRecords();
        Map<Long, List<ProductModelEntity>> models = modelsOf(products.stream()
                .map(ProductEntity::getId).collect(Collectors.toList())).stream()
                .collect(Collectors.groupingBy(ProductModelEntity::getProductId));
        List<ProductExportRow> rows = products.stream().map(product -> {
            ProductExportRow row = new ProductExportRow();
            row.setCategory(product.getCategory() == null ? "" : product.getCategory().getLabel());
            row.setName(product.getName());
            row.setModels(models.getOrDefault(product.getId(), List.of()).stream()
                    .map(ProductModelEntity::getModel).collect(Collectors.joining("、")));
            row.setDescription(product.getDescription());
            row.setCreateTime(product.getCreateTime());
            return row;
        }).collect(Collectors.toList());
        return excelExportService.export("product", RequestContextHolder.get().getTenantId(),
                "产品与型号", ProductExportRow.class, rows);
    }

    /** 型号保存为全量覆盖：新增型号校验标识唯一，移除型号先校验引用。 */
    private void saveModels(Long productId, List<ProductSaveRequest.ModelItem> items) {
        List<ProductModelEntity> current = modelsOf(List.of(productId));
        Set<Long> keepIds = items.stream().map(ProductSaveRequest.ModelItem::getId)
                .filter(java.util.Objects::nonNull).collect(Collectors.toSet());
        for (ProductModelEntity model : current) {
            if (!keepIds.contains(model.getId())) {
                assertModelRemovable(model);
                productModelMapper.deleteById(model.getId());
            }
        }
        Set<String> seen = new HashSet<>();
        for (ProductSaveRequest.ModelItem item : items) {
            String model = item.getModel() == null ? null : item.getModel().trim();
            if (model == null || model.isEmpty()) {
                throw new BizException(ProductErrorCode.PRODUCT_001, "型号标识不能为空");
            }
            if (!seen.add(model)) {
                throw new BizException(ProductErrorCode.PRODUCT_001, "型号标识重复：" + model);
            }
            assertModelUnique(model, item.getId());
            if (item.getId() == null) {
                ProductModelEntity entity = new ProductModelEntity();
                entity.setProductId(productId);
                entity.setModel(model);
                entity.setDeleted(0);
                productModelMapper.insert(entity);
            } else {
                ProductModelEntity entity = productModelMapper.selectById(item.getId());
                if (entity == null) {
                    throw new NotFoundException("型号不存在");
                }
                entity.setModel(model);
                productModelMapper.updateById(entity);
            }
        }
    }

    private void assertModelRemovable(ProductModelEntity model) {
        List<String> blocking = new ArrayList<>();
        referenceProviders.forEach(provider -> blocking.addAll(provider.blockingReferences(model.getId())));
        Long tenantBinding = tenantModelMapper.selectCount(Wrappers.<TenantModelEntity>lambdaQuery()
                .eq(TenantModelEntity::getModelId, model.getId()));
        if (tenantBinding != null && tenantBinding > 0) {
            blocking.add("客户授权 " + tenantBinding + " 个");
        }
        if (!blocking.isEmpty()) {
            throw new BizException(ProductErrorCode.PRODUCT_002,
                    "型号 " + model.getModel() + " 已被 " + String.join("、", blocking) + " 引用，不能移除");
        }
    }

    private void assertModelUnique(String model, Long excludeId) {
        LambdaQueryWrapper<ProductModelEntity> wrapper = Wrappers.<ProductModelEntity>lambdaQuery()
                .eq(ProductModelEntity::getModel, model);
        if (excludeId != null) {
            wrapper.ne(ProductModelEntity::getId, excludeId);
        }
        Long exists = productModelMapper.selectCount(wrapper);
        if (exists != null && exists > 0) {
            throw new BizException(ProductErrorCode.PRODUCT_001, "型号标识已存在：" + model);
        }
    }

    private void assertNameUnique(String name, Long excludeId) {
        LambdaQueryWrapper<ProductEntity> wrapper = Wrappers.<ProductEntity>lambdaQuery()
                .eq(ProductEntity::getName, name);
        if (excludeId != null) {
            wrapper.ne(ProductEntity::getId, excludeId);
        }
        Long exists = productMapper.selectCount(wrapper);
        if (exists != null && exists > 0) {
            throw new BizException(ProductErrorCode.PRODUCT_001, "产品名称已存在");
        }
    }

    private List<ProductModelEntity> modelsOf(List<Long> productIds) {
        if (productIds.isEmpty()) {
            return List.of();
        }
        return productModelMapper.selectList(Wrappers.<ProductModelEntity>lambdaQuery()
                .in(ProductModelEntity::getProductId, productIds));
    }

    private void fillModels(List<ProductVO> rows) {
        if (rows.isEmpty()) {
            return;
        }
        Map<Long, List<ProductModelEntity>> models = modelsOf(rows.stream()
                .map(ProductVO::getId).collect(Collectors.toList())).stream()
                .collect(Collectors.groupingBy(ProductModelEntity::getProductId));
        for (ProductVO row : rows) {
            row.setModels(models.getOrDefault(row.getId(), List.of()).stream().map(model -> {
                ProductModelVO vo = new ProductModelVO();
                vo.setId(model.getId());
                vo.setProductId(model.getProductId());
                vo.setProductName(row.getName());
                vo.setModel(model.getModel());
                return vo;
            }).collect(Collectors.toList()));
        }
    }

    private LambdaQueryWrapper<ProductEntity> buildWrapper(ProductQuery query) {
        LambdaQueryWrapper<ProductEntity> wrapper = Wrappers.lambdaQuery();
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            wrapper.like(ProductEntity::getName, query.getKeyword().trim());
        }
        if (query.getCategory() != null) {
            wrapper.eq(ProductEntity::getCategory, query.getCategory());
        }
        wrapper.orderByDesc(ProductEntity::getCreateTime);
        return wrapper;
    }

    private ProductVO toVO(ProductEntity product) {
        ProductVO vo = new ProductVO();
        vo.setId(product.getId());
        vo.setCategory(product.getCategory() == null ? null : product.getCategory().getCode());
        vo.setCategoryLabel(product.getCategory() == null ? null : product.getCategory().getLabel());
        vo.setName(product.getName());
        vo.setImagePath(product.getImagePath());
        vo.setDescription(product.getDescription());
        vo.setCreateTime(product.getCreateTime());
        return vo;
    }

    private ProductEntity requireProduct(Long id) {
        ProductEntity product = productMapper.selectById(id);
        if (product == null) {
            throw new NotFoundException("产品不存在");
        }
        return product;
    }

    @Data
    public static class ProductExportRow {
        @ExcelProperty("产品类别")
        private String category;
        @ExcelProperty("产品名称")
        private String name;
        @ExcelProperty("型号")
        private String models;
        @ExcelProperty("描述")
        private String description;
        @ExcelProperty("创建时间")
        private LocalDateTime createTime;
    }
}

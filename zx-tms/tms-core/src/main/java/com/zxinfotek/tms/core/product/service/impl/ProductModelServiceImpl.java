package com.zxinfotek.tms.core.product.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zxinfotek.tms.common.exception.BizException;
import com.zxinfotek.tms.common.util.UtcTimes;
import com.zxinfotek.tms.core.product.ProductErrorCode;
import com.zxinfotek.tms.core.product.api.ModelReferenceProvider;
import com.zxinfotek.tms.core.product.api.ProductModelService;
import com.zxinfotek.tms.core.product.api.model.ModelOptionVO;
import com.zxinfotek.tms.core.product.entity.ProductEntity;
import com.zxinfotek.tms.core.product.entity.ProductModelEntity;
import com.zxinfotek.tms.core.product.entity.TenantModelEntity;
import com.zxinfotek.tms.core.product.mapper.ProductMapper;
import com.zxinfotek.tms.core.product.mapper.ProductModelMapper;
import com.zxinfotek.tms.core.product.mapper.TenantModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 型号与客户型号授权：型号用于客户授权、设备入库、内容适配与任务设备筛选（详细设计 3.8）。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
@Service
public class ProductModelServiceImpl implements ProductModelService {

    private static final long PLATFORM_TENANT_ID = 0L;

    private final ProductModelMapper productModelMapper;
    private final ProductMapper productMapper;
    private final TenantModelMapper tenantModelMapper;
    private final List<ModelReferenceProvider> referenceProviders;

    public ProductModelServiceImpl(ProductModelMapper productModelMapper,
                                   ProductMapper productMapper,
                                   TenantModelMapper tenantModelMapper,
                                   List<ModelReferenceProvider> referenceProviders) {
        this.productModelMapper = productModelMapper;
        this.productMapper = productMapper;
        this.tenantModelMapper = tenantModelMapper;
        this.referenceProviders = referenceProviders;
    }

    @Override
    public List<ModelOptionVO> options(Long tenantId) {
        List<ProductModelEntity> models;
        if (tenantId == null || tenantId == PLATFORM_TENANT_ID) {
            models = productModelMapper.selectList(Wrappers.emptyWrapper());
        } else {
            List<Long> modelIds = tenantModelIds(tenantId);
            if (modelIds.isEmpty()) {
                return List.of();
            }
            models = productModelMapper.selectBatchIds(modelIds);
        }
        return toOptions(models);
    }

    @Override
    public List<ModelOptionVO> listByIds(List<Long> modelIds) {
        if (modelIds == null || modelIds.isEmpty()) {
            return List.of();
        }
        return toOptions(productModelMapper.selectBatchIds(modelIds));
    }

    @Override
    public List<Long> tenantModelIds(Long tenantId) {
        return tenantModelMapper.selectList(Wrappers.<TenantModelEntity>lambdaQuery()
                        .eq(TenantModelEntity::getTenantId, tenantId)).stream()
                .map(TenantModelEntity::getModelId).collect(Collectors.toList());
    }

    /**
     * 全量覆盖客户型号授权：取消关联前校验该客户下是否仍有归属设备或业务引用。
     *
     * @author zxinfotek
     * @since 2026-09-18
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTenantModels(Long tenantId, List<Long> modelIds) {
        List<Long> targetIds = modelIds == null ? List.of() : modelIds.stream().distinct().toList();
        List<Long> current = tenantModelIds(tenantId);

        List<Long> removed = current.stream().filter(id -> !targetIds.contains(id)).toList();
        for (Long modelId : removed) {
            List<String> blocking = new ArrayList<>();
            referenceProviders.forEach(provider ->
                    blocking.addAll(provider.blockingReferences(tenantId, modelId)));
            if (!blocking.isEmpty()) {
                throw new BizException(ProductErrorCode.PRODUCT_002,
                        "型号仍有 " + String.join("、", blocking) + "，不能取消关联");
            }
        }
        if (!removed.isEmpty()) {
            tenantModelMapper.delete(Wrappers.<TenantModelEntity>lambdaQuery()
                    .eq(TenantModelEntity::getTenantId, tenantId)
                    .in(TenantModelEntity::getModelId, removed));
        }
        for (Long modelId : targetIds) {
            if (current.contains(modelId)) {
                continue;
            }
            TenantModelEntity entity = new TenantModelEntity();
            entity.setTenantId(tenantId);
            entity.setModelId(modelId);
            entity.setCreateTime(UtcTimes.now());
            tenantModelMapper.insert(entity);
        }
    }

    @Override
    public void removeTenantModels(Long tenantId) {
        tenantModelMapper.delete(Wrappers.<TenantModelEntity>lambdaQuery()
                .eq(TenantModelEntity::getTenantId, tenantId));
    }

    private List<ModelOptionVO> toOptions(List<ProductModelEntity> models) {
        if (models.isEmpty()) {
            return List.of();
        }
        List<Long> productIds = models.stream().map(ProductModelEntity::getProductId)
                .distinct().collect(Collectors.toList());
        Map<Long, ProductEntity> products = productMapper.selectBatchIds(productIds).stream()
                .collect(Collectors.toMap(ProductEntity::getId, product -> product));
        return models.stream().map(model -> {
            ModelOptionVO option = new ModelOptionVO();
            option.setId(model.getId());
            option.setModel(model.getModel());
            ProductEntity product = products.get(model.getProductId());
            option.setProductName(product == null ? null : product.getName());
            option.setCategory(product == null || product.getCategory() == null
                    ? null : product.getCategory().getCode());
            return option;
        }).collect(Collectors.toList());
    }
}

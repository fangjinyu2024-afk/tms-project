package com.zxinfotek.tms.core.product.api;

import com.zxinfotek.tms.core.product.api.model.ModelOptionVO;

import java.util.List;

public interface ProductModelService {

    /** 型号下拉：平台返回全部型号，客户按其型号授权过滤。 */
    List<ModelOptionVO> options(Long tenantId);

    List<ModelOptionVO> listByIds(List<Long> modelIds);

    List<Long> tenantModelIds(Long tenantId);

    /** 全量覆盖客户型号授权，取消关联时校验引用。 */
    void saveTenantModels(Long tenantId, List<Long> modelIds);

    void removeTenantModels(Long tenantId);
}

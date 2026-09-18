package com.zxinfotek.tms.core.iam.api;

import com.zxinfotek.tms.common.model.PageResult;
import com.zxinfotek.tms.core.iam.api.model.StatusRequest;
import com.zxinfotek.tms.core.iam.api.model.TenantCreateResultVO;
import com.zxinfotek.tms.core.iam.api.model.TenantFeatureSaveRequest;
import com.zxinfotek.tms.core.iam.api.model.TenantFeatureVO;
import com.zxinfotek.tms.core.iam.api.model.TenantQuery;
import com.zxinfotek.tms.core.iam.api.model.TenantSaveRequest;
import com.zxinfotek.tms.core.iam.api.model.TenantVO;

public interface TenantService {

    PageResult<TenantVO> page(TenantQuery query);

    TenantVO detail(Long id);

    TenantCreateResultVO create(TenantSaveRequest request);

    void update(Long id, TenantSaveRequest request);

    void changeStatus(Long id, StatusRequest request);

    void delete(Long id);

    TenantFeatureVO features(Long id);

    /** 客户功能授权的可选菜单，新增客户时用于渲染勾选项。 */
    java.util.List<TenantFeatureVO.MenuOptionVO> featureOptions();

    TenantFeatureVO saveFeatures(Long id, TenantFeatureSaveRequest request);

    String export(TenantQuery query);

    /** 平台租户与其功能授权的初始化，服务启动时调用。 */
    void ensurePlatformTenant();

    String tenantName(Long tenantId);
}

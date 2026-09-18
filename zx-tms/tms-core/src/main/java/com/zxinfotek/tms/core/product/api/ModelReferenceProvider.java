package com.zxinfotek.tms.core.product.api;

import java.util.List;

/**
 * 型号引用校验扩展点，由设备、升级包等模块实现并注入。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
public interface ModelReferenceProvider {

    /** 返回该客户下引用了指定型号的说明，如「设备 12 台」；无引用返回空集合。 */
    List<String> blockingReferences(Long tenantId, Long modelId);

    /** 返回引用了指定型号的说明（不限客户）。 */
    List<String> blockingReferences(Long modelId);
}

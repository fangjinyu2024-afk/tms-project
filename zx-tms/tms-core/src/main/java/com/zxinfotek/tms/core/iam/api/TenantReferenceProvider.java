package com.zxinfotek.tms.core.iam.api;

import java.util.List;

/**
 * 客户删除前的跨模块引用校验扩展点，由设备、任务等模块实现并注入。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
public interface TenantReferenceProvider {

    /** 返回阻断删除的引用说明，如「任务 3 个」；无引用返回空集合。 */
    List<String> blockingReferences(Long tenantId);
}

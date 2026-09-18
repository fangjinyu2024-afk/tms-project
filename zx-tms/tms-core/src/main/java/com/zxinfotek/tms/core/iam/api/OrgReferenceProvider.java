package com.zxinfotek.tms.core.iam.api;

import java.util.List;

/**
 * 机构删除前的跨模块引用校验扩展点，由设备、任务等模块实现并注入，避免 iam 反向依赖业务模块。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
public interface OrgReferenceProvider {

    /** 返回阻断删除的引用说明，如「设备 12 台」；无引用返回空集合。 */
    List<String> blockingReferences(Long orgId, String orgPath);
}

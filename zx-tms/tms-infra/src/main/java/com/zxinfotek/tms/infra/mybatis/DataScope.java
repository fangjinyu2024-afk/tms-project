package com.zxinfotek.tms.infra.mybatis;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在 Mapper 方法上，由 SQL 拦截器按会话上下文追加租户与机构范围条件（详细设计 7.1）。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataScope {

    /** 租户列，空串表示该表不做租户过滤 */
    String tenantColumn() default "tenant_id";

    /** 机构列，配合 SELF_ORG 使用 */
    String orgIdColumn() default "org_id";

    /** 机构路径列，配合 ORG_AND_SUB 使用 */
    String orgPathColumn() default "org_path";
}

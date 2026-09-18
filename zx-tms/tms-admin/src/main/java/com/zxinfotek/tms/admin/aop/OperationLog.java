package com.zxinfotek.tms.admin.aop;

import com.zxinfotek.tms.common.enums.LogModule;
import com.zxinfotek.tms.common.enums.OperAction;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明该接口对应一次可审计的业务操作，由切面在方法结束后写入操作日志（详细设计 7.5）。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {

    LogModule module();

    OperAction action();

    String objectType() default "";

    /** 变更摘要字段白名单，未在白名单内的字段不进入摘要 */
    String[] summaryFields() default {};
}

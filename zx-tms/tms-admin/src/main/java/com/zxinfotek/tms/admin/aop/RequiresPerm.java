package com.zxinfotek.tms.admin.aop;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明接口所需的权限码，任何业务接口都必须声明，未声明的接口不允许合入（详细设计 7.1）。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPerm {

    /** 权限码，格式为「菜单键:操作」 */
    String value() default "";

    /** 满足其中任一权限码即可访问，用于同一入口服务多个菜单的场景 */
    String[] anyOf() default {};
}

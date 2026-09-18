package com.zxinfotek.tms.core.arch;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * 跨模块与跨域约束的自动化校验，对应详细设计 7.12 与 CLAUDE.md 的跨模块红线。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
class ModuleDependencyTest {

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.zxinfotek.tms");

    @Test
    @DisplayName("tms-task 不得依赖 tms-core")
    void taskMustNotDependOnCore() {
        ArchRule rule = noClasses().that().resideInAPackage("com.zxinfotek.tms.task..")
                .should().dependOnClassesThat().resideInAPackage("com.zxinfotek.tms.core..");
        rule.allowEmptyShould(true).check(CLASSES);
    }

    @Test
    @DisplayName("跨域只允许引用对方 api 包：iam 不得触达 audit、product 的实体与实现")
    void iamMustOnlyUseOtherDomainApi() {
        ArchRule rule = noClasses().that().resideInAPackage("com.zxinfotek.tms.core.iam..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "com.zxinfotek.tms.core.audit.entity..",
                        "com.zxinfotek.tms.core.audit.mapper..",
                        "com.zxinfotek.tms.core.audit.service..",
                        "com.zxinfotek.tms.core.product.entity..",
                        "com.zxinfotek.tms.core.product.mapper..",
                        "com.zxinfotek.tms.core.product.service..");
        rule.allowEmptyShould(true).check(CLASSES);
    }

    @Test
    @DisplayName("跨域只允许引用对方 api 包：audit 不得触达 iam、product 的实体与实现")
    void auditMustOnlyUseOtherDomainApi() {
        ArchRule rule = noClasses().that().resideInAPackage("com.zxinfotek.tms.core.audit..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "com.zxinfotek.tms.core.iam.entity..",
                        "com.zxinfotek.tms.core.iam.mapper..",
                        "com.zxinfotek.tms.core.iam.service..",
                        "com.zxinfotek.tms.core.product.entity..",
                        "com.zxinfotek.tms.core.product.mapper..",
                        "com.zxinfotek.tms.core.product.service..");
        rule.allowEmptyShould(true).check(CLASSES);
    }

    @Test
    @DisplayName("跨域只允许引用对方 api 包：product 不得触达 iam、audit 的实体与实现")
    void productMustOnlyUseOtherDomainApi() {
        ArchRule rule = noClasses().that().resideInAPackage("com.zxinfotek.tms.core.product..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "com.zxinfotek.tms.core.iam.entity..",
                        "com.zxinfotek.tms.core.iam.mapper..",
                        "com.zxinfotek.tms.core.iam.service..",
                        "com.zxinfotek.tms.core.audit.entity..",
                        "com.zxinfotek.tms.core.audit.mapper..",
                        "com.zxinfotek.tms.core.audit.service..");
        rule.allowEmptyShould(true).check(CLASSES);
    }

    @Test
    @DisplayName("tms-common 与 tms-infra 不得反向依赖业务模块")
    void foundationMustNotDependOnBusiness() {
        ArchRule rule = noClasses().that().resideInAnyPackage(
                        "com.zxinfotek.tms.common..", "com.zxinfotek.tms.infra..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "com.zxinfotek.tms.core..", "com.zxinfotek.tms.task..",
                        "com.zxinfotek.tms.activation..", "com.zxinfotek.tms.ota..",
                        "com.zxinfotek.tms.rki..", "com.zxinfotek.tms.admin..",
                        "com.zxinfotek.tms.gateway..");
        rule.allowEmptyShould(true).check(CLASSES);
    }

    @Test
    @DisplayName("实体不直接暴露给接口层：VO / DTO 不得引用实体")
    void apiModelMustNotExposeEntity() {
        ArchRule rule = noClasses().that().resideInAPackage("com.zxinfotek.tms.core..api..")
                .should().dependOnClassesThat().resideInAPackage("com.zxinfotek.tms.core..entity..");
        rule.allowEmptyShould(true).check(CLASSES);
    }
}

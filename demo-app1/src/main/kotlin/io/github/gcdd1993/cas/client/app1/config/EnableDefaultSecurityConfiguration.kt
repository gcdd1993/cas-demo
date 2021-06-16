package io.github.gcdd1993.cas.client.app1.config

import org.springframework.context.annotation.Import

@Retention(value = AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@MustBeDocumented
@Import(
    DefaultSecurityConfiguration::class, DefaultPatternController::class
)
annotation class EnableDefaultSecurityConfiguration(
    /**
     * 仅限调试用，使全部接口跳过认证
     */
    val disableAuth: Boolean = false,
    /**
     * 仅限调试用，对CAS服务的调用禁用SSL证书检查
     */
    val disableSslCheck: Boolean = false
)
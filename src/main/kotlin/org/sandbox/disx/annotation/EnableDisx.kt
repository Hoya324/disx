package org.sandbox.disx.annotation

import org.sandbox.disx.config.DisxAutoConfiguration
import org.springframework.context.annotation.Import

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Import(DisxAutoConfiguration::class)
annotation class EnableDisx

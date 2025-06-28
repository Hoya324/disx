package org.sandbox.disx.annotation


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class DomainEvent(
    val name: String = "",
    val exchange: String = "disx.events",
    val routingKey: String = "",
    val priority: Int = 0,
    val persistent: Boolean = true
)
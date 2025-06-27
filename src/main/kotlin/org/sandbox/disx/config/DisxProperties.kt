package org.sandbox.disx.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "disx")
data class DisxProperties(
    val enabled: Boolean = true,
    val rabbitmq: RabbitMQProperties = RabbitMQProperties(),
    val redis: RedisProperties = RedisProperties(),
    val outbox: OutboxProperties = OutboxProperties()
) {
    data class RabbitMQProperties(
        val host: String = "localhost",
        val port: Int = 5672,
        val username: String = "guest",
        val password: String = "guest"
    )

    data class RedisProperties(
        val host: String = "localhost",
        val port: Int = 6379,
        val password: String = "",
        val database: Int = 0
    )

    data class OutboxProperties(
        val enabled: Boolean = true
    )
}


package org.sandbox.disx.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "disx")
data class DisxProperties(
    val enabled: Boolean = true,
    val kafka: KafkaProperties = KafkaProperties(),
    val outbox: OutboxProperties = OutboxProperties()
) {

    data class KafkaProperties(
        val bootstrapServers: String = "localhost:9092",
        val partitions: Int = 3,
        val replicas: Int = 1
    )

    data class OutboxProperties(
        val enabled: Boolean = true,
        val pollingInterval: Long = 1000,
        val batchSize: Int = 100
    )
}


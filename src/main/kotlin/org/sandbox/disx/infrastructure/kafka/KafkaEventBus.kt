package org.sandbox.disx.infrastructure.kafka

import org.sandbox.disx.core.event.BaseDomainEvent
import org.sandbox.disx.core.event.EventBus
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class KafkaEventBus : EventBus {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun <T : BaseDomainEvent> publish(event: T) {
        logger.debug("🔄 Event publish intercepted by Outbox pattern: ${event.eventId}")
    }

    override suspend fun <T : BaseDomainEvent> publishAsync(event: T) {
        publish(event)
    }

    override fun <T : BaseDomainEvent> publishBatch(events: List<T>) {
        events.forEach { publish(it) }
    }

    override suspend fun <T : BaseDomainEvent> publishBatchAsync(events: List<T>) {
        publishBatch(events)
    }
}

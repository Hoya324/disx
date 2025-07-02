package org.sandbox.disx.core.outbox

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicReference

@Component
@ConditionalOnProperty(prefix = "disx.outbox", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class OutboxKafkaPublisher(
    private val outboxRepository: OutboxRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val lastProcessedEventId = AtomicReference<String?>(null)
    private val defaultTopic = "disx-events"

    init {
        lastProcessedEventId.set(outboxRepository.getLastProcessedEventId())
    }

    @Scheduled(fixedDelay = 1000)
    fun publishEvents() {
        try {
            val events = outboxRepository.findPendingEventsAfterId(lastProcessedEventId.get(), 100)

            if (events.isEmpty()) {
                return
            }

            logger.debug("📤 Publishing ${events.size} events to Kafka")

            events.forEach { event ->
                publishToKafka(event)
            }

            // update last Processed EventId
            events.lastOrNull()?.let {
                lastProcessedEventId.set(it.id)
            }

        } catch (e: Exception) {
            logger.error("❌ Failed to publish events to Kafka", e)
        }
    }

    private fun publishToKafka(event: OutboxEvent) {
        try {
            val topic = determineTopicFromEventType(event.eventType)

            val record = ProducerRecord(
                topic,
                null,
                event.createdAt.toEpochMilli(),
                event.aggregateId,
                event.eventData
            ).apply {
                headers().add("eventId", event.id.toByteArray())
                headers().add("eventType", event.eventType.toByteArray())
                headers().add("aggregateId", event.aggregateId.toByteArray())
            }

            kafkaTemplate.send(record).whenComplete { result, ex ->
                if (ex == null) {
                    logger.debug("✅ Event published to Kafka: ${event.id} -> ${result?.recordMetadata?.topic()}")
                    outboxRepository.updateStatus(event.id, OutboxStatus.PROCESSED)
                } else {
                    logger.error("❌ Failed to publish event to Kafka: ${event.id}", ex)
                    outboxRepository.updateStatus(event.id, OutboxStatus.FAILED)
                }
            }

        } catch (e: Exception) {
            logger.error("❌ Error publishing event ${event.id} to Kafka", e)
            outboxRepository.updateStatus(event.id, OutboxStatus.FAILED)
        }
    }

    // TODO: Make this configurable dynamically
    private fun determineTopicFromEventType(eventType: String): String {
        return when {
            eventType.contains("Order", ignoreCase = true) -> "order-events"
            eventType.contains("Payment", ignoreCase = true) -> "payment-events"
            eventType.contains("Inventory", ignoreCase = true) -> "inventory-events"
            else -> defaultTopic
        }
    }
}
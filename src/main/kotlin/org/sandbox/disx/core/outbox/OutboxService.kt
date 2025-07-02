package org.sandbox.disx.core.outbox

import com.fasterxml.jackson.databind.ObjectMapper
import org.sandbox.disx.core.event.BaseDomainEvent
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import java.util.*

@Service
class OutboxService(
    private val repository: OutboxRepository,
    private val objectMapper: ObjectMapper,
    private val applicationContext: ApplicationContext
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Save domain event to Outbox
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    fun handleDomainEvent(event: BaseDomainEvent) {
        val outboxEvent = OutboxEvent(
            id = UUID.randomUUID().toString(),
            aggregateId = event.aggregateId,
            eventType = event::class.qualifiedName ?: "UnknownEvent",
            eventData = objectMapper.writeValueAsString(event)
        )

        repository.save(outboxEvent)
        logger.debug("📥 Outbox event saved: ${outboxEvent.id}")
    }
}
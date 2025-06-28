package org.sandbox.disx.outbox

import java.time.Instant

open class OutboxEvent(
    open val id: String,
    open val aggregateId: String,
    open val eventType: String,
    open val eventData: String,
    open val status: OutboxStatus = OutboxStatus.PENDING,
    open val createdAt: Instant = Instant.now(),
    open val processedAt: Instant? = null,
    open val retryCount: Int = 0
) {
    fun markAsProcessed(): OutboxEvent {
        return OutboxEvent(
            id,
            aggregateId,
            eventType,
            eventData,
            OutboxStatus.PROCESSED,
            createdAt,
            Instant.now(),
            retryCount
        )
    }

    fun markAsFailed(): OutboxEvent {
        return OutboxEvent(
            id,
            aggregateId,
            eventType,
            eventData,
            OutboxStatus.FAILED,
            createdAt,
            processedAt,
            retryCount + 1
        )
    }

    override fun toString(): String {
        return "OutboxEvent(id='$id', aggregateId='$aggregateId', eventType='$eventType', status=$status)"
    }
}
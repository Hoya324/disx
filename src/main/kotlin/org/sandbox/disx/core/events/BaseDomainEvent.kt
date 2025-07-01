package org.sandbox.disx.core.events

import java.time.Instant
import java.util.*


abstract class BaseDomainEvent(
    val aggregateId: String,
    val eventId: String = UUID.randomUUID().toString(),
    val occurredAt: Instant = Instant.now()
) {

    /**
     * Returns the event type.
     */
    fun getEventType(): String = this::class.simpleName ?: "UnknownEvent"

    /**
     * Returns a string representation of the event.
     */
    override fun toString(): String {
        return "${getEventType()}(aggregateId=$aggregateId, eventId=$eventId, occurredAt=$occurredAt)"
    }

    /**
     * Checks the equality of events.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BaseDomainEvent) return false
        return eventId == other.eventId
    }

    /**
     * Returns the hash code of the event.
     */
    override fun hashCode(): Int {
        return eventId.hashCode()
    }
}

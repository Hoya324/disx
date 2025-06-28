package org.sandbox.disx.core.events

import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.Instant
import java.util.*

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
abstract class BaseDomainEvent(
    val aggregateId: String,
    val eventId: String = UUID.randomUUID().toString(),
    val occurredAt: Instant = Instant.now(),
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BaseDomainEvent) return false
        return eventId == other.eventId
    }

    override fun hashCode(): Int = eventId.hashCode()

    override fun toString(): String {
        return "${this::class.simpleName}(eventId='$eventId', aggregateId='$aggregateId')"
    }
}
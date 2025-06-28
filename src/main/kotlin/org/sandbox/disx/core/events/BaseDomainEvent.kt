package org.sandbox.disx.core.events

import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.Instant
import java.util.*

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
abstract class BaseDomainEvent(
    val aggregateId: String,
    val eventId: String = UUID.randomUUID().toString(),
    val occurredAt: Instant = Instant.now(),
    private val _metadata: MutableMap<String, Any> = mutableMapOf()
) {

    val metadata: Map<String, Any> get() = _metadata.toMap()

    fun addMetadata(key: String, value: Any): BaseDomainEvent {
        _metadata[key] = value
        return this
    }

    fun getMetadata(key: String): Any? = _metadata[key]

    private inline fun <reified T> getMetadata(key: String): T? = _metadata[key] as? T

    fun setCorrelationId(correlationId: String): BaseDomainEvent {
        return addMetadata("correlationId", correlationId)
    }

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
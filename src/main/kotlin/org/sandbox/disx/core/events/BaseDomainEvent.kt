package org.sandbox.disx.core.events

import java.time.Instant
import java.util.*


abstract class BaseDomainEvent(
    val aggregateId: String,
    val eventId: String = UUID.randomUUID().toString(),
    val occurredAt: Instant = Instant.now()
) {

    /**
     * 이벤트 타입을 반환합니다.
     */
    fun getEventType(): String = this::class.simpleName ?: "UnknownEvent"

    /**
     * 이벤트를 문자열로 표현합니다.
     */
    override fun toString(): String {
        return "${getEventType()}(aggregateId=$aggregateId, eventId=$eventId, occurredAt=$occurredAt)"
    }

    /**
     * 이벤트의 동등성을 확인합니다.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BaseDomainEvent) return false
        return eventId == other.eventId
    }

    /**
     * 이벤트의 해시 코드를 반환합니다.
     */
    override fun hashCode(): Int {
        return eventId.hashCode()
    }
}

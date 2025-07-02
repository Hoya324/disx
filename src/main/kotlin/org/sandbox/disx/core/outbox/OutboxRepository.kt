package org.sandbox.disx.core.outbox

interface OutboxRepository {

    /**
     * Save event (called within transaction)
     */
    fun save(event: OutboxEvent)

    /**
     * Find pending events
     */
    fun findPendingEvents(limit: Int = 100): List<OutboxEvent>

    /**
     * Update event status
     */
    fun updateStatus(eventId: String, status: OutboxStatus)

    /**
     * Get last processed event ID
     */
    fun getLastProcessedEventId(): String?

    /**
     * Find pending events after specific ID
     */
    fun findPendingEventsAfterId(lastEventId: String?, limit: Int = 100): List<OutboxEvent>
}

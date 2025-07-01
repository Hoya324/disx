package org.sandbox.disx.core.events

/**
 * Event bus interface
 */
interface EventBus {
    
    /**
     * Publishes an event synchronously.
     */
    fun <T : BaseDomainEvent> publish(event: T)
    
    /**
     * Publishes an event asynchronously.
     */
    suspend fun <T : BaseDomainEvent> publishAsync(event: T)
    
    /**
     * Publishes multiple events synchronously in batch.
     */
    fun <T : BaseDomainEvent> publishBatch(events: List<T>)
    
    /**
     * Publishes multiple events asynchronously in batch.
     */
    suspend fun <T : BaseDomainEvent> publishBatchAsync(events: List<T>)
}

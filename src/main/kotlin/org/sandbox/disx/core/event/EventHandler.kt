package org.sandbox.disx.core.event

interface EventHandler<T : BaseDomainEvent> {

    /**
     * Process the event.
     *
     * @param event Event to process
     */
    suspend fun handle(event: T)

    /**
     * Returns the event type this handler can process.
     */
    fun getEventType(): Class<T>
}

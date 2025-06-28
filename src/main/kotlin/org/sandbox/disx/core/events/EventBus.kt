package org.sandbox.disx.core.events

interface EventBus {

    /**
     * 단일 이벤트를 동기적으로 발행합니다.
     */
    fun <T : BaseDomainEvent> publish(event: T)

    /**
     * 단일 이벤트를 비동기적으로 발행합니다.
     */
    suspend fun <T : BaseDomainEvent> publishAsync(event: T)

    /**
     * 여러 이벤트를 배치로 발행합니다.
     */
    fun <T : BaseDomainEvent> publishBatch(events: List<T>)

    /**
     * 여러 이벤트를 비동기 배치로 발행합니다.
     */
    suspend fun <T : BaseDomainEvent> publishBatchAsync(events: List<T>)
}

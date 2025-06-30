package org.sandbox.disx.core.events

/**
 * 이벤트 버스 인터페이스
 */
interface EventBus {
    
    /**
     * 이벤트를 동기적으로 발행합니다.
     */
    fun <T : BaseDomainEvent> publish(event: T)
    
    /**
     * 이벤트를 비동기적으로 발행합니다.
     */
    suspend fun <T : BaseDomainEvent> publishAsync(event: T)
    
    /**
     * 여러 이벤트를 동기적으로 배치 발행합니다.
     */
    fun <T : BaseDomainEvent> publishBatch(events: List<T>)
    
    /**
     * 여러 이벤트를 비동기적으로 배치 발행합니다.
     */
    suspend fun <T : BaseDomainEvent> publishBatchAsync(events: List<T>)
}

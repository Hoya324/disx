package org.sandbox.disx.core.events

interface EventHandler<T : BaseDomainEvent> {

    /**
     * 이벤트를 처리합니다.
     *
     * @param event 처리할 이벤트
     */
    suspend fun handle(event: T)

    /**
     * 이 핸들러가 처리할 수 있는 이벤트 타입을 반환합니다.
     */
    fun getEventType(): Class<T>
}

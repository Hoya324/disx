package org.sandbox.disx.outbox

import org.springframework.stereotype.Component

@Component
interface OutboxRepository {

    /**
     * 이벤트 저장 (트랜잭션 내에서 호출됨)
     */
    fun save(event: OutboxEvent)

    /**
     * 처리 대기 중인 이벤트 조회
     */
    fun findPendingEvents(limit: Int = 100): List<OutboxEvent>

    /**
     * 이벤트 상태 업데이트
     */
    fun updateStatus(eventId: String, status: OutboxStatus)

    /**
     * 마지막 처리한 이벤트 ID 조회
     */
    fun getLastProcessedEventId(): String?

    /**
     * 특정 ID 이후의 대기 중인 이벤트 조회
     */
    fun findPendingEventsAfterId(lastEventId: String?, limit: Int = 100): List<OutboxEvent>
}

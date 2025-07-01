package org.sandbox.disx.core.events

import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class BaseDomainEventTest {

    private class TestDomainEvent(
        aggregateId: String,
        eventId: String = UUID.randomUUID().toString(),
        occurredAt: Instant = Instant.now()
    ) : BaseDomainEvent(aggregateId, eventId, occurredAt)

    @Test
    fun `이벤트 생성시 기본값이 올바르게 설정되는지 테스트`() {
        val aggregateId = "test-aggregate-123"
        val event = TestDomainEvent(aggregateId)
        
        assertEquals(aggregateId, event.aggregateId)
        assertTrue(event.eventId.isNotEmpty())
        assertTrue(event.occurredAt.isBefore(Instant.now().plusSeconds(1)))
        assertTrue(event.occurredAt.isAfter(Instant.now().minusSeconds(1)))
    }

    @Test
    fun `이벤트 생성시 커스텀 값이 올바르게 설정되는지 테스트`() {
        val aggregateId = "test-aggregate-456"
        val customEventId = "custom-event-id"
        val customOccurredAt = Instant.now().minusSeconds(10)
        
        val event = TestDomainEvent(aggregateId, customEventId, customOccurredAt)
        
        assertEquals(aggregateId, event.aggregateId)
        assertEquals(customEventId, event.eventId)
        assertEquals(customOccurredAt, event.occurredAt)
    }

    @Test
    fun `getEventType이 올바른 클래스명을 반환하는지 테스트`() {
        val event = TestDomainEvent("test-aggregate")
        
        assertEquals("TestDomainEvent", event.getEventType())
    }

    @Test
    fun `toString이 올바른 형식으로 출력되는지 테스트`() {
        val aggregateId = "test-aggregate"
        val eventId = "test-event-id"
        val occurredAt = Instant.parse("2024-01-01T00:00:00Z")
        
        val event = TestDomainEvent(aggregateId, eventId, occurredAt)
        val expectedString = "TestDomainEvent(aggregateId=test-aggregate, eventId=test-event-id, occurredAt=2024-01-01T00:00:00Z)"
        
        assertEquals(expectedString, event.toString())
    }

    @Test
    fun `같은 eventId를 가진 이벤트들은 동등해야 함`() {
        val eventId = "same-event-id"
        val event1 = TestDomainEvent("aggregate1", eventId)
        val event2 = TestDomainEvent("aggregate2", eventId)
        
        assertEquals(event1, event2)
        assertEquals(event1.hashCode(), event2.hashCode())
    }

    @Test
    fun `다른 eventId를 가진 이벤트들은 동등하지 않아야 함`() {
        val event1 = TestDomainEvent("aggregate1", "event-id-1")
        val event2 = TestDomainEvent("aggregate1", "event-id-2")
        
        assertNotEquals(event1, event2)
        assertNotEquals(event1.hashCode(), event2.hashCode())
    }

    @Test
    fun `대량의 이벤트 생성 성능 테스트`() {
        val eventCount = 100000
        val aggregateId = "performance-test-aggregate"
        
        val executionTime = kotlin.system.measureTimeMillis {
            repeat(eventCount) {
                TestDomainEvent(aggregateId)
            }
        }
        
        println("대량 이벤트 생성 - 이벤트 수: $eventCount, 실행 시간: ${executionTime}ms")
        println("평균 생성 시간: ${executionTime.toDouble() / eventCount}ms/event")
        
        // 성능 검증: 100,000개 이벤트를 1초 이내에 생성 가능해야 함
        assertTrue(executionTime < 1000, "이벤트 생성이 너무 느립니다: ${executionTime}ms")
    }

    @Test
    fun `이벤트 ID 유니크성 테스트`() {
        val eventCount = 10000
        val events = (1..eventCount).map { TestDomainEvent("aggregate-$it") }
        val uniqueEventIds = events.map { it.eventId }.toSet()
        
        assertEquals(eventCount, uniqueEventIds.size, "생성된 이벤트 ID가 모두 유니크해야 합니다")
    }

    @Test
    fun `이벤트 생성 시간 순서 테스트`() {
        val events = mutableListOf<TestDomainEvent>()
        
        repeat(100) {
            events.add(TestDomainEvent("aggregate-$it"))
            Thread.sleep(1) // 1ms 대기로 시간 차이 보장
        }
        
        // 생성 시간이 순차적으로 증가하는지 확인
        for (i in 1 until events.size) {
            assertTrue(
                events[i].occurredAt.isAfter(events[i-1].occurredAt) || 
                events[i].occurredAt.equals(events[i-1].occurredAt),
                "이벤트 생성 시간이 순서대로 되어있지 않습니다"
            )
        }
    }
}

package org.sandbox.disx.core.event

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EventHandlerTest {

    private class TestEvent(
        aggregateId: String,
        val data: String = "test-data",
        eventId: String = UUID.randomUUID().toString(),
        occurredAt: Instant = Instant.now()
    ) : BaseDomainEvent(aggregateId, eventId, occurredAt)

    private class CountingEventHandler : EventHandler<TestEvent> {
        val handledCount = AtomicInteger(0)

        override suspend fun handle(event: TestEvent) {
            handledCount.incrementAndGet()
        }

        override fun getEventType(): Class<TestEvent> = TestEvent::class.java
    }

    private class ExceptionThrowingHandler : EventHandler<TestEvent> {
        val attemptCount = AtomicInteger(0)

        override suspend fun handle(event: TestEvent) {
            attemptCount.incrementAndGet()
            throw RuntimeException("Test exception")
        }

        override fun getEventType(): Class<TestEvent> = TestEvent::class.java
    }

    @Test
    fun `이벤트 핸들러 기본 동작 테스트`() = runBlocking {
        val handler = CountingEventHandler()
        val event = TestEvent("test-aggregate", "test-data")

        handler.handle(event)

        assertEquals(1, handler.handledCount.get())
        assertEquals(TestEvent::class.java, handler.getEventType())
    }

    @Test
    fun `여러 이벤트 순차 처리 테스트`() = runBlocking {
        val handler = CountingEventHandler()
        val eventCount = 10

        repeat(eventCount) { i ->
            val event = TestEvent("aggregate-$i", "data-$i")
            handler.handle(event)
        }

        assertEquals(eventCount, handler.handledCount.get())
    }

    @Test
    fun `예외 발생 핸들러 테스트`() = runBlocking {
        val handler = ExceptionThrowingHandler()
        val event = TestEvent("test-aggregate")

        try {
            handler.handle(event)
            assertTrue(false, "예외가 발생해야 합니다")
        } catch (e: RuntimeException) {
            assertEquals("Test exception", e.message)
            assertEquals(1, handler.attemptCount.get())
        }
    }

    @Test
    fun `이벤트 타입 확인 테스트`() {
        val handler = CountingEventHandler()
        assertEquals(TestEvent::class.java, handler.getEventType())
    }

    @Test
    fun `동일한 핸들러로 다른 이벤트 처리 테스트`() = runBlocking {
        val handler = CountingEventHandler()

        val event1 = TestEvent("aggregate-1", "data-1")
        val event2 = TestEvent("aggregate-2", "data-2")
        val event3 = TestEvent("aggregate-3", "data-3")

        handler.handle(event1)
        handler.handle(event2)
        handler.handle(event3)

        assertEquals(3, handler.handledCount.get())
    }
}

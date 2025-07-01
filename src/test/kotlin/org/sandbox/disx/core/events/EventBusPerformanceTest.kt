package org.sandbox.disx.core.events

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EventBusSimpleTest {

    private class TestEvent(
        aggregateId: String,
        val data: String = "test-data",
        eventId: String = UUID.randomUUID().toString(),
        occurredAt: Instant = Instant.now()
    ) : BaseDomainEvent(aggregateId, eventId, occurredAt)

    private class TestEventHandler : EventHandler<TestEvent> {
        val processedCount = AtomicLong(0)
        
        override suspend fun handle(event: TestEvent) {
            processedCount.incrementAndGet()
        }
        
        override fun getEventType(): Class<TestEvent> = TestEvent::class.java
    }

    // 테스트용 간단한 EventBus 구현체
    private class TestEventBusImpl : EventBus {
        private val handlers = mutableMapOf<Class<out BaseDomainEvent>, MutableList<EventHandler<out BaseDomainEvent>>>()
        
        override fun <T : BaseDomainEvent> publish(event: T) {
            runBlocking {
                getHandlersForEvent(event::class.java).forEach { handler ->
                    @Suppress("UNCHECKED_CAST")
                    (handler as EventHandler<BaseDomainEvent>).handle(event)
                }
            }
        }
        
        override suspend fun <T : BaseDomainEvent> publishAsync(event: T) {
            getHandlersForEvent(event::class.java).forEach { handler ->
                @Suppress("UNCHECKED_CAST")
                (handler as EventHandler<BaseDomainEvent>).handle(event)
            }
        }
        
        override fun <T : BaseDomainEvent> publishBatch(events: List<T>) {
            runBlocking {
                events.forEach { publishAsync(it) }
            }
        }
        
        override suspend fun <T : BaseDomainEvent> publishBatchAsync(events: List<T>) {
            events.forEach { publishAsync(it) }
        }
        
        fun <T : BaseDomainEvent> registerHandler(eventType: Class<T>, handler: EventHandler<T>) {
            handlers.computeIfAbsent(eventType) { mutableListOf() }
                .add(handler as EventHandler<out BaseDomainEvent>)
        }
        
        private fun getHandlersForEvent(eventType: Class<out BaseDomainEvent>): List<EventHandler<out BaseDomainEvent>> {
            return handlers[eventType] ?: emptyList()
        }
    }

    @Test
    fun `이벤트 버스 기본 동작 테스트`() {
        val eventBus = TestEventBusImpl()
        val handler = TestEventHandler()
        eventBus.registerHandler(TestEvent::class.java, handler)
        
        val event = TestEvent("test-aggregate")
        eventBus.publish(event)
        
        assertEquals(1, handler.processedCount.get())
    }

    @Test
    fun `배치 이벤트 발행 테스트`() {
        val eventBus = TestEventBusImpl()
        val handler = TestEventHandler()
        eventBus.registerHandler(TestEvent::class.java, handler)
        
        val events = listOf(
            TestEvent("aggregate-1"),
            TestEvent("aggregate-2"),
            TestEvent("aggregate-3")
        )
        
        eventBus.publishBatch(events)
        
        assertEquals(3, handler.processedCount.get())
    }

    @Test
    fun `비동기 이벤트 발행 테스트`() = runBlocking {
        val eventBus = TestEventBusImpl()
        val handler = TestEventHandler()
        eventBus.registerHandler(TestEvent::class.java, handler)
        
        val event = TestEvent("test-aggregate")
        eventBus.publishAsync(event)
        
        assertEquals(1, handler.processedCount.get())
    }

    @Test
    fun `핸들러가 없는 이벤트 발행 테스트`() {
        val eventBus = TestEventBusImpl()
        val event = TestEvent("test-aggregate")
        
        // 예외가 발생하지 않아야 함
        eventBus.publish(event)
        assertTrue(true)
    }

    @Test
    fun `여러 핸들러 등록 테스트`() {
        val eventBus = TestEventBusImpl()
        val handler1 = TestEventHandler()
        val handler2 = TestEventHandler()
        
        eventBus.registerHandler(TestEvent::class.java, handler1)
        eventBus.registerHandler(TestEvent::class.java, handler2)
        
        val event = TestEvent("test-aggregate")
        eventBus.publish(event)
        
        assertEquals(1, handler1.processedCount.get())
        assertEquals(1, handler2.processedCount.get())
    }
}

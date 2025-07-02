package org.sandbox.disx.core.event

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
    fun `test that default values are set correctly when creating an event`() {
        val aggregateId = "test-aggregate-123"
        val event = TestDomainEvent(aggregateId)

        assertEquals(aggregateId, event.aggregateId)
        assertTrue(event.eventId.isNotEmpty())
        assertTrue(event.occurredAt.isBefore(Instant.now().plusSeconds(1)))
        assertTrue(event.occurredAt.isAfter(Instant.now().minusSeconds(1)))
    }

    @Test
    fun `test that custom values are set correctly when creating an event`() {
        val aggregateId = "test-aggregate-456"
        val customEventId = "custom-event-id"
        val customOccurredAt = Instant.now().minusSeconds(10)

        val event = TestDomainEvent(aggregateId, customEventId, customOccurredAt)

        assertEquals(aggregateId, event.aggregateId)
        assertEquals(customEventId, event.eventId)
        assertEquals(customOccurredAt, event.occurredAt)
    }

    @Test
    fun `test that getEventType returns the correct class name`() {
        val event = TestDomainEvent("test-aggregate")

        assertEquals("TestDomainEvent", event.getEventType())
    }

    @Test
    fun `test that toString outputs in the correct format`() {
        val aggregateId = "test-aggregate"
        val eventId = "test-event-id"
        val occurredAt = Instant.parse("2024-01-01T00:00:00Z")

        val event = TestDomainEvent(aggregateId, eventId, occurredAt)
        val expectedString =
            "TestDomainEvent(aggregateId=test-aggregate, eventId=test-event-id, occurredAt=2024-01-01T00:00:00Z)"

        assertEquals(expectedString, event.toString())
    }

    @Test
    fun `events with the same eventId should be equal`() {
        val eventId = "same-event-id"
        val event1 = TestDomainEvent("aggregate1", eventId)
        val event2 = TestDomainEvent("aggregate2", eventId)

        assertEquals(event1, event2)
        assertEquals(event1.hashCode(), event2.hashCode())
    }

    @Test
    fun `events with different eventIds should not be equal`() {
        val event1 = TestDomainEvent("aggregate1", "event-id-1")
        val event2 = TestDomainEvent("aggregate1", "event-id-2")

        assertNotEquals(event1, event2)
        assertNotEquals(event1.hashCode(), event2.hashCode())
    }

    @Test
    fun `bulk event creation performance test`() {
        val eventCount = 100000
        val aggregateId = "performance-test-aggregate"

        val executionTime = kotlin.system.measureTimeMillis {
            repeat(eventCount) {
                TestDomainEvent(aggregateId)
            }
        }

        println("Bulk event creation - Event count: $eventCount, Execution time: ${executionTime}ms")
        println("Average creation time: ${executionTime.toDouble() / eventCount}ms/event")

        // Performance validation: Should be able to create 100,000 events within 1 second
        assertTrue(executionTime < 1000, "Event creation is too slow: ${executionTime}ms")
    }

    @Test
    fun `event ID uniqueness test`() {
        val eventCount = 10000
        val events = (1..eventCount).map { TestDomainEvent("aggregate-$it") }
        val uniqueEventIds = events.map { it.eventId }.toSet()

        assertEquals(eventCount, uniqueEventIds.size, "All generated event IDs should be unique")
    }

    @Test
    fun `event creation time order test`() {
        val events = mutableListOf<TestDomainEvent>()

        repeat(100) {
            events.add(TestDomainEvent("aggregate-$it"))
            Thread.sleep(1) // 1ms wait to ensure time difference
        }

        // Check if creation times are in sequential order
        for (i in 1 until events.size) {
            assertTrue(
                events[i].occurredAt.isAfter(events[i - 1].occurredAt) ||
                        events[i].occurredAt.equals(events[i - 1].occurredAt),
                "Event creation times are not in order"
            )
        }
    }
}

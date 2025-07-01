# Disx

> Distributed Event Streaming Framework / Supports Kafka and transactional outbox pattern.

![jdk 17](https://img.shields.io/badge/minimum_jdk-17-orange?labelColor=black&style=flat-square) ![spring-boot](https://img.shields.io/badge/spring--boot-3.2.x-brightgreen?labelColor=black&style=flat-square)    
![kotlin](https://img.shields.io/badge/kotlin-1.9.21-purple?labelColor=black&style=flat-square) ![kafka](https://img.shields.io/badge/-apache--kafka-000000?style=flat-square&logo=Apache%20Kafka&logoColor=white)

**High-throughput event processing** with guaranteed delivery using transactional outbox pattern.

Disx is a distributed event streaming framework that provides the following features:

1. **Transactional Outbox Pattern** - Automatically ensures reliable event publishing with database transactions.
2. **Kafka Integration** - Native support for Apache Kafka with Spring Kafka.
3. **Event-Driven Architecture** - Domain events with automatic serialization and routing.
4. **Coroutines Support** - Async event handling with Kotlin coroutines.
5. **Spring Boot Auto-Configuration** - Zero-configuration setup with `@EnableDisx`.
6. **At-Least-Once Delivery** - Guarantees message delivery with retry mechanisms.
7. **AOP-based Event Interception** - Automatic outbox pattern implementation.

## Table of Contents

- [Download](#download)
- [How to use](#how-to-use)
    - [Basic Setup](#basic-setup)
    - [Configuration](#configuration)
    - [Creating Domain Events](#creating-domain-events)
    - [Publishing Events](#publishing-events)
    - [Event Handlers](#event-handlers)
    - [Outbox Pattern](#outbox-pattern)
- [Repository Implementation](#repository-implementation)
- [Testing](#testing)

## Download

### Gradle

```kotlin
dependencies {
    implementation("org.sandbox:disx:0.0.9")
}
```

### Maven

```xml

<dependency>
    <groupId>org.sandbox</groupId>
    <artifactId>disx</artifactId>
    <version>0.0.9</version>
</dependency>
```

## How to use

### Basic Setup

Enable Disx in your Spring Boot application:

```kotlin
@EnableDisx
@SpringBootApplication
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
```

### Configuration

Configure Disx using application properties:

```yaml
disx:
  enabled: true
  kafka:
    bootstrap-servers: localhost:9092
    partitions: 3
    replicas: 1
  outbox:
    enabled: true
    polling-interval: 1000
    batch-size: 100

spring:
  kafka:
    producer:
      acks: all
      retries: 3
```

#### Properties

| Property                       | Default          | Description                   |
|--------------------------------|------------------|-------------------------------|
| `disx.enabled`                 | `true`           | Enable/disable Disx framework |
| `disx.kafka.bootstrap-servers` | `localhost:9092` | Kafka broker addresses        |
| `disx.kafka.partitions`        | `3`              | Default partitions for topics |
| `disx.kafka.replicas`          | `1`              | Default replication factor    |
| `disx.outbox.enabled`          | `true`           | Enable outbox pattern         |
| `disx.outbox.polling-interval` | `1000`           | Polling interval in ms        |
| `disx.outbox.batch-size`       | `100`            | Batch size for processing     |

### Creating Domain Events

Define your domain events by extending `BaseDomainEvent`:

```kotlin
@DomainEvent(
    name = "order.created",
    exchange = "disx.events",
    routingKey = "order.created",
    priority = 1
)
data class OrderCreatedEvent(
    val customerId: String,
    val amount: BigDecimal,
    val items: List<OrderItem>
) : BaseDomainEvent(
    aggregateId = "order-${UUID.randomUUID()}",
    eventId = UUID.randomUUID().toString(),
    occurredAt = Instant.now()
)
```

The `@DomainEvent` annotation supports:

- `name`: Event name identifier
- `exchange`: Message exchange (default: "disx.events")
- `routingKey`: Routing key for message routing
- `priority`: Event priority (default: 0)
- `persistent`: Message persistence (default: true)

### Publishing Events

#### Synchronous Publishing

```kotlin
@Service
class OrderService(private val eventBus: EventBus) {

    @Transactional
    fun createOrder(customerId: String, amount: BigDecimal): String {
        val orderId = UUID.randomUUID().toString()

        // Business logic
        val order = saveOrder(customerId, amount)

        // Publish event - automatically intercepted by outbox pattern
        eventBus.publish(OrderCreatedEvent(customerId, amount))

        return orderId
    }
}
```

#### Asynchronous Publishing

```kotlin
@Service
class PaymentService(private val eventBus: EventBus) {

    suspend fun processPayment(orderId: String, amount: BigDecimal) {
        // Process payment logic

        // Publish async event
        eventBus.publishAsync(
            PaymentProcessedEvent(orderId, amount, "credit-card")
        )
    }
}
```

#### Batch Publishing

```kotlin
@Service
class BulkOrderService(private val eventBus: EventBus) {

    @Transactional
    fun processBulkOrders(orders: List<CreateOrderRequest>) {
        val events = orders.map { request ->
            val order = createOrder(request)
            OrderCreatedEvent(order.customerId, order.amount)
        }

        // Batch publish for better performance
        eventBus.publishBatch(events)

        // Or async batch
        // eventBus.publishBatchAsync(events)
    }
}
```

### Event Handlers

Create event handlers by implementing the `EventHandler` interface:

```kotlin
@Component
class OrderEventHandler : EventHandler<OrderCreatedEvent> {

    override suspend fun handle(event: OrderCreatedEvent) {
        // Process the order created event
        println("Processing order: ${event.aggregateId}")

        // Send notification
        notificationService.sendOrderConfirmation(event.customerId)

        // Update inventory
        inventoryService.reserveItems(event.items)
    }

    override fun getEventType(): Class<OrderCreatedEvent> {
        return OrderCreatedEvent::class.java
    }
}
```

### Outbox Pattern

The outbox pattern is automatically implemented through AOP. When you publish an event within a transaction:

1. **Event Interception**: The `OutboxEventPublisher` intercepts all `EventBus.publish*()` calls
2. **Transaction Detection**: Checks if a Spring transaction is active
3. **Event Storage**: If transaction exists, converts to Spring event and stores in outbox
4. **Background Publishing**: `OutboxKafkaPublisher` polls the outbox and publishes to Kafka

The `OutboxEvent` class represents events in the outbox:

```kotlin
data class OutboxEvent(
    val id: String,
    val aggregateId: String,
    val eventType: String,
    val eventData: String,
    val status: OutboxStatus = OutboxStatus.PENDING,
    val createdAt: Instant = Instant.now(),
    val processedAt: Instant? = null,
    val retryCount: Int = 0
)

enum class OutboxStatus {
    PENDING,
    PROCESSED,
    FAILED
}
```

## Repository Implementation

You need to implement the `OutboxRepository` interface for your persistence layer:

```kotlin
@Repository
class JpaOutboxRepository(
    private val jpaRepository: OutboxEventJpaRepository
) : OutboxRepository {

    override fun save(event: OutboxEvent) {
        jpaRepository.save(event.toEntity())
    }

    override fun findPendingEvents(limit: Int): List<OutboxEvent> {
        return jpaRepository.findByStatusOrderByCreatedAt(
            OutboxStatus.PENDING,
            PageRequest.of(0, limit)
        ).map { it.toDomain() }
    }

    override fun updateStatus(eventId: String, status: OutboxStatus) {
        jpaRepository.updateStatus(eventId, status, Instant.now())
    }

    override fun getLastProcessedEventId(): String? {
        return jpaRepository.findFirstByStatusOrderByProcessedAtDesc(
            OutboxStatus.PROCESSED
        )?.id
    }

    override fun findPendingEventsAfterId(lastEventId: String?, limit: Int): List<OutboxEvent> {
        return if (lastEventId != null) {
            jpaRepository.findPendingEventsAfterId(lastEventId, limit)
        } else {
            jpaRepository.findByStatusOrderByCreatedAt(
                OutboxStatus.PENDING,
                PageRequest.of(0, limit)
            )
        }.map { it.toDomain() }
    }
}
```

### Database Schema

Example schema for the outbox table:

```sql
CREATE TABLE outbox_events
(
    id           VARCHAR(255) PRIMARY KEY,
    aggregate_id VARCHAR(255) NOT NULL,
    event_type   VARCHAR(500) NOT NULL,
    event_data   TEXT         NOT NULL,
    status       VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    processed_at TIMESTAMP NULL,
    retry_count  INTEGER      NOT NULL DEFAULT 0,

    INDEX        idx_status_created (status, created_at),
    INDEX        idx_aggregate_id (aggregate_id),
    INDEX        idx_processed_at (processed_at)
);
```

## Testing

### Unit Testing Events

```kotlin
class OrderCreatedEventTest {

    @Test
    fun `should create order event with correct properties`() {
        val customerId = "customer-123"
        val amount = BigDecimal("99.99")

        val event = OrderCreatedEvent(customerId, amount, emptyList())

        assertThat(event.customerId).isEqualTo(customerId)
        assertThat(event.amount).isEqualTo(amount)
        assertThat(event.aggregateId).isNotEmpty()
        assertThat(event.eventId).isNotEmpty()
        assertThat(event.occurredAt).isBeforeOrEqualTo(Instant.now())
    }

    @Test
    fun `events with same eventId should be equal`() {
        val eventId = "same-event-id"
        val event1 = OrderCreatedEvent("customer1", BigDecimal.TEN, emptyList())
            .copy(eventId = eventId)
        val event2 = OrderCreatedEvent("customer2", BigDecimal.ONE, emptyList())
            .copy(eventId = eventId)

        assertThat(event1).isEqualTo(event2)
        assertThat(event1.hashCode()).isEqualTo(event2.hashCode())
    }
}
```

### Integration Testing

```kotlin
@SpringBootTest
@TestPropertySource(
    properties = [
        "disx.kafka.bootstrap-servers=\${spring.embedded.kafka.brokers}",
        "disx.outbox.enabled=true"
    ]
)
@EmbeddedKafka(partitions = 1, topics = ["test-events"])
class DisxIntegrationTest {

    @Autowired
    private lateinit var eventBus: EventBus

    @Autowired
    private lateinit var outboxRepository: OutboxRepository

    @Test
    @Transactional
    fun `should save event to outbox when published within transaction`() {
        val event = OrderCreatedEvent("customer-123", BigDecimal("99.99"), emptyList())

        eventBus.publish(event)

        val outboxEvents = outboxRepository.findPendingEvents()
        assertThat(outboxEvents).hasSize(1)
        assertThat(outboxEvents.first().eventType).contains("OrderCreatedEvent")
    }
}
```

## Architecture

### Event Flow

1. **Business Logic** → Calls `eventBus.publish(event)` within `@Transactional` method
2. **AOP Interception** → `OutboxEventPublisher` intercepts the call
3. **Transaction Check** → Verifies if Spring transaction is active
4. **Event Storage** → Converts to Spring event, handled by `OutboxService`
5. **Outbox Persistence** → Event saved to database within same transaction
6. **Background Publishing** → `OutboxKafkaPublisher` polls and publishes to Kafka
7. **Status Update** → Event status updated to PROCESSED/FAILED

### Key Components

- **`@EnableDisx`** - Enables auto-configuration
- **`EventBus`** - Core interface for publishing events
- **`BaseDomainEvent`** - Base class for all domain events
- **`@DomainEvent`** - Annotation for event metadata
- **`OutboxEventPublisher`** - AOP aspect for transaction interception
- **`OutboxService`** - Handles Spring events and outbox storage
- **`OutboxKafkaPublisher`** - Background publisher to Kafka
- **`OutboxRepository`** - Interface for outbox persistence

## Contributing

Contributions are welcome! Please read our [Contributing Guide](CONTRIBUTING.md) for details.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

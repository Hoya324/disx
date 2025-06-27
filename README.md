# Disx - Kotlin Distributed Event Framework

🚀 **분산환경을 경험해보고 싶지만 시스템 구축은 귀찮은 개발자들을 위한 프레임워크**

## ✨ 주요 기능

- 🎯 **이벤트 기반 아키텍처**: RabbitMQ 기반 안정적인 이벤트 처리
- 🔒 **분산 락**: Redis 기반 다양한 락 타입 지원
- 🔄 **Saga 패턴**: Orchestration/Choreography 지원
- 📊 **모니터링**: Prometheus, Grafana 연동
- 🛡️ **안정성**: 자동 재시도, DLQ, 멱등성 보장
- ⚡ **고성능**: 비동기 처리, 백프레셔 제어

## 🚀 빠른 시작

### 1. 의존성 추가

```kotlin
dependencies {
    implementation("io.github.disx:disx:1.0.0")
}
```

### 2. 프레임워크 활성화

```kotlin
@SpringBootApplication
@EnableDisx
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
```

### 3. 첫 번째 이벤트

```kotlin
@DomainEvent(name = "order.created")
data class OrderCreatedEvent(
    val orderId: String,
    val customerId: String,
    val amount: BigDecimal
) : BaseDomainEvent(orderId)

@Service
class OrderService(private val eventBus: EventBus) {
    fun createOrder(customerId: String, amount: BigDecimal): String {
        val orderId = UUID.randomUUID().toString()
        eventBus.publish(OrderCreatedEvent(orderId, customerId, amount))
        return orderId
    }
}

@Component
class NotificationService {
    @EventHandler(events = [OrderCreatedEvent::class])
    fun sendNotification(event: OrderCreatedEvent) {
        println("📧 주문 확인 이메일 발송: ${event.orderId}")
    }
}
```

## 📖 문서

- [사용자 가이드](https://disx.io/docs)
- [API 레퍼런스](https://disx.io/api)
- [예제 프로젝트](https://github.com/disx/examples)

## 🤝 기여하기

버그 리포트, 기능 요청, Pull Request를 환영합니다!

## 📄 라이선스

Apache License 2.0
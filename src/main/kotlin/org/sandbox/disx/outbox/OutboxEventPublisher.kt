package org.sandbox.disx.outbox

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.sandbox.disx.core.events.BaseDomainEvent
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionSynchronizationManager

@Aspect
@Component
class OutboxEventPublisher(
    private val applicationContext: ApplicationContext
) {

    @Around("execution(* org.sandbox.disx.core.events.EventBus.publish*(..))")
    fun interceptPublish(joinPoint: ProceedingJoinPoint): Any? {
        val event = joinPoint.args[0] as? BaseDomainEvent

        if (event != null && isTransactionActive()) {
            // 스프링 이벤트로 발행
            applicationContext.publishEvent(event)
            return null
        }

        // 트랜잭션이 없으면 그대로 발행
        return joinPoint.proceed()
    }

    private fun isTransactionActive(): Boolean {
        return TransactionSynchronizationManager.isActualTransactionActive()
    }
}
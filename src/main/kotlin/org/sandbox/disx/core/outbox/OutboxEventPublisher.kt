package org.sandbox.disx.core.outbox

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.sandbox.disx.core.event.BaseDomainEvent
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionSynchronizationManager

@Aspect
@Component
class OutboxEventPublisher(
    private val applicationContext: ApplicationContext
) {

    @Around("execution(* org.sandbox.disx.core.event.EventBus.publish*(..))")
    fun interceptPublish(joinPoint: ProceedingJoinPoint): Any? {
        val event = joinPoint.args[0] as? BaseDomainEvent

        if (event != null && isTransactionActive()) {
            // Publish as Spring event
            applicationContext.publishEvent(event)
            return null
        }

        // If no transaction, publish as is
        return joinPoint.proceed()
    }

    private fun isTransactionActive(): Boolean {
        return TransactionSynchronizationManager.isActualTransactionActive()
    }
}
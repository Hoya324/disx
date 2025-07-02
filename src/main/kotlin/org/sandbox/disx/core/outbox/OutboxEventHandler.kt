package org.sandbox.disx.core.outbox

interface OutboxEventHandler {

    suspend fun handle(event: OutboxEvent)

    fun canHandle(eventType: String): Boolean
}

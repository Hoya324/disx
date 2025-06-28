package org.sandbox.disx.outbox

interface OutboxEventHandler {

    suspend fun handle(event: OutboxEvent)

    fun canHandle(eventType: String): Boolean
}

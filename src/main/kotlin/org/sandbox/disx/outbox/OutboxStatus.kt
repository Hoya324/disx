package org.sandbox.disx.outbox

enum class OutboxStatus {
    PENDING,
    PROCESSED,
    FAILED
}
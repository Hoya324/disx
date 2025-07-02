package org.sandbox.disx.core.outbox

enum class OutboxStatus {
    PENDING,
    PROCESSED,
    FAILED
}
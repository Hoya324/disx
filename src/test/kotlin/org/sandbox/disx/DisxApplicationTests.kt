package org.sandbox.disx

import org.junit.jupiter.api.Test
import org.sandbox.disx.core.outbox.OutboxRepository
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean

@SpringBootTest
class DisxApplicationTests {

    @MockBean
    private lateinit var outboxRepository: OutboxRepository

    @Test
    fun contextLoads() {
    }

}

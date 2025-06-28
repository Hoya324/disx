package org.sandbox.disx.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.sandbox.disx.core.events.EventBus
import org.sandbox.disx.infrastructure.kafka.KafkaEventBus
import org.sandbox.disx.outbox.OutboxEventPublisher
import org.sandbox.disx.outbox.OutboxKafkaPublisher
import org.sandbox.disx.outbox.OutboxRepository
import org.sandbox.disx.outbox.OutboxService
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@ConditionalOnProperty(
    prefix = "disx",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true
)
@EnableConfigurationProperties(DisxProperties::class)
@EnableAspectJAutoProxy
@EnableScheduling
class DisxAutoConfiguration(
    private val properties: DisxProperties
) {

    @Bean
    @ConditionalOnMissingBean
    fun objectMapper(): ObjectMapper {
        return ObjectMapper().apply {
            registerModule(KotlinModule.Builder().build())
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = ["org.springframework.kafka.core.KafkaTemplate"])
    fun kafkaAdmin(): KafkaAdmin {
        val configs = mutableMapOf<String, Any>()
        configs[AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG] = properties.kafka.bootstrapServers
        return KafkaAdmin(configs)
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = ["org.springframework.kafka.core.KafkaTemplate"])
    fun producerFactory(): ProducerFactory<String, String> {
        val configs = mutableMapOf<String, Any>()
        configs[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = properties.kafka.bootstrapServers
        configs[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        configs[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        configs[ProducerConfig.ACKS_CONFIG] = "all"
        configs[ProducerConfig.RETRIES_CONFIG] = 3
        configs[ProducerConfig.BATCH_SIZE_CONFIG] = 16384
        configs[ProducerConfig.LINGER_MS_CONFIG] = 10
        configs[ProducerConfig.BUFFER_MEMORY_CONFIG] = 33554432
        configs[ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG] = true

        return DefaultKafkaProducerFactory(configs)
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = ["org.springframework.kafka.core.KafkaTemplate"])
    fun kafkaTemplate(producerFactory: ProducerFactory<String, String>): KafkaTemplate<String, String> {
        return KafkaTemplate(producerFactory)
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = ["org.springframework.kafka.core.KafkaTemplate"])
    fun eventBus(): EventBus {
        return KafkaEventBus()
    }

    // Kafka Topics
    @Bean
    fun disxEventsTopic(): NewTopic {
        return TopicBuilder.name("disx-events")
            .partitions(properties.kafka.partitions)
            .replicas(properties.kafka.replicas)
            .build()
    }

    @Bean
    fun orderEventsTopic(): NewTopic {
        return TopicBuilder.name("order-events")
            .partitions(properties.kafka.partitions)
            .replicas(properties.kafka.replicas)
            .build()
    }

    @Bean
    fun paymentEventsTopic(): NewTopic {
        return TopicBuilder.name("payment-events")
            .partitions(properties.kafka.partitions)
            .replicas(properties.kafka.replicas)
            .build()
    }

    @Bean
    fun inventoryEventsTopic(): NewTopic {
        return TopicBuilder.name("inventory-events")
            .partitions(properties.kafka.partitions)
            .replicas(properties.kafka.replicas)
            .build()
    }

    @Bean
    @ConditionalOnMissingBean
    fun outboxService(
        repository: OutboxRepository,
        objectMapper: ObjectMapper,
        applicationContext: ApplicationContext
    ): OutboxService {
        return OutboxService(repository, objectMapper, applicationContext)
    }

    @Bean
    @ConditionalOnMissingBean
    fun outboxEventPublisher(applicationContext: ApplicationContext): OutboxEventPublisher {
        return OutboxEventPublisher(applicationContext)
    }

    @Bean
    @ConditionalOnMissingBean
    fun outboxKafkaPublisher(
        outboxRepository: OutboxRepository,
        kafkaTemplate: KafkaTemplate<String, String>,
        objectMapper: ObjectMapper
    ): OutboxKafkaPublisher {
        return OutboxKafkaPublisher(outboxRepository, kafkaTemplate, objectMapper)
    }
}

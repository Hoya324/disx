package org.sandbox.disx.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.lettuce.core.event.EventBus
import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.springframework.amqp.core.AmqpAdmin
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@ConditionalOnProperty(
    prefix = "disx",
    name = ["enable"],
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

    // RabbitMQ 구성
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = ["org.springframework.amqp.rabbit.core.RabbitTemplate"])
    fun connectionFactory(): CachingConnectionFactory {
        return CachingConnectionFactory().apply {
            this.setHost(properties.rabbitmq.host)
            this.setPassword(properties.rabbitmq.password)
            this.port = properties.rabbitmq.port
            this.username = properties.rabbitmq.username
        }
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = ["org.springframework.amqp.rabbit.core.RabbitTemplate"])
    fun messageConverter(objectMapper: ObjectMapper): MessageConverter {
        return Jackson2JsonMessageConverter(objectMapper)
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = ["org.springframework.amqp.rabbit.core.RabbitTemplate"])
    fun rabbitTemplate(
        connectionFactory: ConnectionFactory,
        messageConverter: MessageConverter
    ): RabbitTemplate {
        return RabbitTemplate(connectionFactory).apply {
            this.messageConverter = messageConverter
        }
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = ["org.springframework.amqp.rabbit.core.RabbitTemplate"])
    fun amqpAdmin(connectionFactory: ConnectionFactory): AmqpAdmin {
        return RabbitAdmin(connectionFactory)
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = ["org.springframework.amqp.rabbit.core.RabbitTemplate"])
    fun eventBus(
        rabbitTemplate: RabbitTemplate,
        amqpAdmin: AmqpAdmin
    ): EventBus {
        return RabbitMQEventBus(rabbitTemplate, amqpAdmin)
    }

    // Redis 구성
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = ["org.redisson.api.RedissonClient"])
    fun redissonClient(): RedissonClient {
        val config = Config()
        config.useSingleServer()
            .setAddress("redis://${properties.redis.host}:${properties.redis.port}")
            .setPassword(properties.redis.password.takeIf { it.isNotBlank() })
            .setDatabase(properties.redis.database)

        return Redisson.create(config)
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = ["org.redisson.api.RedissonClient"])
    fun lockManager(redissonClient: RedissonClient): LockManager {
        return LockManager(redissonClient)
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = ["org.redisson.api.RedissonClient"])
    fun distributedLockAspect(lockManager: LockManager): DistributedLockAspect {
        return DistributedLockAspect(lockManager)
    }

    // Outbox 구성
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
    fun outboxProcessor(outboxService: OutboxService): OutboxProcessor {
        return OutboxProcessor(outboxService)
    }
}
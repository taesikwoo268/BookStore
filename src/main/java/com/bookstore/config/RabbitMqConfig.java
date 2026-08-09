package com.bookstore.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Value("${rabbitmq.exchange.order}")
    private String orderExchange;

    @Value("${rabbitmq.queue.order-placed}")
    private String orderPlacedQueue;

    @Value("${rabbitmq.queue.inventory}")
    private String inventoryQueue;

    @Value("${rabbitmq.queue.email}")
    private String emailQueue;

    @Value("${rabbitmq.queue.loyalty}")
    private String loyaltyQueue;

    @Value("${rabbitmq.queue.notification}")
    private String notificationQueue;

    @Value("${rabbitmq.routing-key.order-placed}")
    private String orderPlacedRoutingKey;

    @Value("${rabbitmq.queue.dead-letter}")
    private String deadLetterQueue;

    // ============================================================
    // 1. EXCHANGE
    // ============================================================

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(orderExchange, true, false);
    }

    // ============================================================
    // 2. QUEUES
    // ============================================================

    @Bean
    public Queue orderPlacedQueue() {
        return new Queue(orderPlacedQueue, true);
    }

    @Bean
    public Queue inventoryQueue() {
        return new Queue(inventoryQueue, true);
    }

    @Bean
    public Queue emailQueue() {
        return new Queue(emailQueue, true);
    }

    @Bean
    public Queue loyaltyQueue() {
        return new Queue(loyaltyQueue, true);
    }

    @Bean
    public Queue notificationQueue() {
        return new Queue(notificationQueue, true);
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue(deadLetterQueue, true);
    }

    // ============================================================
    // 3. BINDINGS
    // ============================================================

    @Bean
    public Binding orderPlacedBinding() {
        return BindingBuilder
                .bind(orderPlacedQueue())
                .to(orderExchange())
                .with(orderPlacedRoutingKey);
    }

    @Bean
    public Binding inventoryBinding() {
        return BindingBuilder
                .bind(inventoryQueue())
                .to(orderExchange())
                .with("order.placed.inventory");
    }

    @Bean
    public Binding emailBinding() {
        return BindingBuilder
                .bind(emailQueue())
                .to(orderExchange())
                .with("order.placed.email");
    }

    @Bean
    public Binding loyaltyBinding() {
        return BindingBuilder
                .bind(loyaltyQueue())
                .to(orderExchange())
                .with("order.placed.loyalty");
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(orderExchange())
                .with("order.placed.notification");
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(orderExchange())
                .with("order.dead-letter");
    }


    // ============================================================
    // 4. MESSAGE CONVERTER
    // ============================================================

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }

}

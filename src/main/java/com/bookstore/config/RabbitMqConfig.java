package com.bookstore.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMqConfig {

    @Value("${rabbitmq.exchange.order}")
    private String orderExchange;

    @Value("${rabbitmq.queue.inventory}")
    private String inventoryQueue;

    @Value("${rabbitmq.queue.inventory-dlq}")
    private String inventoryDlq;

    @Value("${rabbitmq.queue.email}")
    private String emailQueue;

    @Value("${rabbitmq.queue.email-dlq}")
    private String emailDlq;

    @Value("${rabbitmq.queue.loyalty}")
    private String loyaltyQueue;

    @Value("${rabbitmq.queue.loyalty-dlq}")
    private String loyaltyDlq;

    @Value("${rabbitmq.routing-key.order-placed}")
    private String orderPlacedRoutingKey;

    // ============================================================
    // 1. EXCHANGE
    // ============================================================

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(orderExchange, true, false);
    }

    // ============================================================
    // 2. DEAD LETTER EXCHANGE
    // ============================================================

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange("order.dead-letter.exchange", true, false);
    }

    // ============================================================
    // 3. INVENTORY QUEUE + DLQ
    // ============================================================

    @Bean
    public Queue inventoryDlq() {
        return QueueBuilder.durable(inventoryDlq)
                .build();
    }

    @Bean
    public Queue inventoryQueue() {
        Map<String, Object> args = new HashMap<>();
        // ✅ Set dead letter exchange
        args.put("x-dead-letter-exchange", "order.dead-letter.exchange");
        // ✅ Set dead letter routing key
        args.put("x-dead-letter-routing-key", inventoryDlq);

        return QueueBuilder.durable(inventoryQueue)
                .withArguments(args)
                .build();
    }

    @Bean
    public Binding inventoryBinding() {
        return BindingBuilder
                .bind(inventoryQueue())
                .to(orderExchange())
                .with("order.placed.inventory");
    }

    @Bean
    public Binding inventoryDlqBinding() {
        return BindingBuilder
                .bind(inventoryDlq())
                .to(deadLetterExchange())
                .with(inventoryDlq);
    }

    // ============================================================
    // 4. EMAIL QUEUE + DLQ
    // ============================================================

    @Bean
    public Queue emailDlq() {
        return QueueBuilder.durable(emailDlq).build();
    }

    @Bean
    public Queue emailQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", "order.dead-letter.exchange");
        args.put("x-dead-letter-routing-key", emailDlq);

        return QueueBuilder.durable(emailQueue)
                .withArguments(args)
                .build();
    }

    @Bean
    public Binding emailBinding() {
        return BindingBuilder
                .bind(emailQueue())
                .to(orderExchange())
                .with("order.placed.email");
    }

    @Bean
    public Binding emailDlqBinding() {
        return BindingBuilder
                .bind(emailDlq())
                .to(deadLetterExchange())
                .with(emailDlq);
    }

    // ============================================================
    // 5. LOYALTY QUEUE + DLQ
    // ============================================================

    @Bean
    public Queue loyaltyDlq() {
        return QueueBuilder.durable(loyaltyDlq).build();
    }

    @Bean
    public Queue loyaltyQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", "order.dead-letter.exchange");
        args.put("x-dead-letter-routing-key", loyaltyDlq);

        return QueueBuilder.durable(loyaltyQueue)
                .withArguments(args)
                .build();
    }

    @Bean
    public Binding loyaltyBinding() {
        return BindingBuilder
                .bind(loyaltyQueue())
                .to(orderExchange())
                .with("order.placed.loyalty");
    }

    @Bean
    public Binding loyaltyDlqBinding() {
        return BindingBuilder
                .bind(loyaltyDlq())
                .to(deadLetterExchange())
                .with(loyaltyDlq);
    }

    // ============================================================
    // 6. MESSAGE CONVERTER
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
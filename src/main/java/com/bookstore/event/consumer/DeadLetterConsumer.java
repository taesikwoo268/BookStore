package com.bookstore.event.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeadLetterConsumer {

    @RabbitListener(
            queues = "${rabbitmq.queue.dead-letter}",
            autoStartup = "false"
    )
    public void handleDeadLetter(byte[] message) {
        log.error("💀 [DeadLetterConsumer] Received dead letter message: {}", new String(message));
        // Thêm logic xử lý lỗi (lưu vào DB, gửi alert, retry...)
    }
}
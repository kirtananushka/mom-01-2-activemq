package com.tananushka.mom.requesterapp.requester;

import jakarta.jms.JMSException;
import jakarta.jms.TextMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class Requester {
    private final JmsTemplate jmsTemplate;
    @Value("${spring.jms.queue-name}")
    private String queueName;

    @Scheduled(fixedRateString = "${spring.jms.scheduler.fixed-rate}")
    public void sendAndReceiveMessage() {
        String messageContent = "Request at " + LocalDateTime.now();
        MessageCreator messageCreator = session -> {
            TextMessage message = session.createTextMessage(messageContent);
            String jmsCorrelationId = UUID.randomUUID().toString();
            message.setJMSCorrelationID(jmsCorrelationId);
            message.setJMSReplyTo(session.createTemporaryQueue());
            log.info(">>> Message sent with correlation ID {}: {}", jmsCorrelationId, messageContent);
            return message;
        };

        TextMessage reply = (TextMessage) jmsTemplate.sendAndReceive(queueName, messageCreator);
        try {
            String replyText = reply != null ? reply.getText() : "No reply received";
            String jmsCorrelationId = reply != null ? reply.getJMSCorrelationID() : "No jmsCorrelationId received";
            log.info("<<< Reply received for correlation ID {}: {}", jmsCorrelationId, replyText);
        } catch (JMSException e) {
            log.error("Error receiving reply", e);
        }
    }
}

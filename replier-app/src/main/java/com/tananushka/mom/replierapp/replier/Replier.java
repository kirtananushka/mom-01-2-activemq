package com.tananushka.mom.replierapp.replier;

import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class Replier {
    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = "${spring.jms.queue-name}")
    public void processRequest(Message message) throws JMSException {
        String requestContent = message.getBody(String.class);
        Destination replyTo = message.getJMSReplyTo();
        String responseContent = "Processed: " + requestContent;
        jmsTemplate.send(replyTo, session -> {
            TextMessage reply = session.createTextMessage(responseContent);
            reply.setJMSCorrelationID(message.getJMSCorrelationID());
            return reply;
        });
        log.info(">>> Sent reply to {}: {}", message.getJMSCorrelationID(), responseContent);
    }
}



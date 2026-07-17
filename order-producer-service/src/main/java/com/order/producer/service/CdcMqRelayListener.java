package com.order.producer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.order.producer.entity.OutboxEvent;
import com.order.producer.model.OrderMessage;
import com.order.producer.repository.OutboxEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@Slf4j
public class CdcMqRelayListener {

    @Autowired
    private OrderProducer orderProducer;

    @Autowired
    private OutboxEventRepository outboxRepo;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "order_producer.public.outbox_events", groupId = "producer-relay-group")
    public void handleProducerOutbox(String message) {
        processEvent(message, "Producer");
    }

    @KafkaListener(topics = "order_inventory.public.outbox_events", groupId = "inventory-relay-group")
    public void handleInventoryOutbox(String message) {
        processEvent(message, "Inventory");
    }

    private void processEvent(String message, String source) {
        try {
            log.info("CDC Event from {}: {}", source, message);
            JsonNode root = objectMapper.readTree(message);
            JsonNode payloadNode = root.get("payload");
            if (payloadNode == null || payloadNode.get("after") == null || payloadNode.get("after").isNull()) {
                return;
            }

            JsonNode after = payloadNode.get("after");
            String topic = after.get("topic").asText();
            String payload = after.get("payload").asText();
            
            OrderMessage order = objectMapper.readValue(payload, OrderMessage.class);
            orderProducer.sendOrder(order);

            // If it's the producer's own outbox, we might want to update the status
            if ("Producer".equals(source)) {
                Long id = after.get("id").asLong();
                Optional<OutboxEvent> eventOpt = outboxRepo.findById(id);
                eventOpt.ifPresent(event -> {
                    event.setStatus("SENT");
                    event.setSentAt(LocalDateTime.now());
                    outboxRepo.save(event);
                });
            }

            log.info("Relayed {} event for orderId={} to MQ topic={}", source, order.getOrderId(), topic);

        } catch (Exception e) {
            log.error("Failed to relay {} CDC event: {}", source, e.getMessage());
        }
    }
}

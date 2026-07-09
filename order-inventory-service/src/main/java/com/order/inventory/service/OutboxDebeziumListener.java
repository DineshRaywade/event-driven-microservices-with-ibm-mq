package com.order.inventory.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.order.inventory.model.OrderMessage;
import com.order.inventory.repository.OutboxEventRepository;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.Json;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class OutboxDebeziumListener {

    @Autowired
    private io.debezium.config.Configuration debeziumConnectorConfig;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private OutboxEventRepository outboxRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private ExecutorService executor;

    private DebeziumEngine<ChangeEvent<String, String>> engine;

    @PostConstruct
    public void start() {

        executor = Executors.newSingleThreadExecutor();

        engine =
                DebeziumEngine.create(Json.class)
                        .using(
                                debeziumConnectorConfig.asProperties()
                        )
                        .notifying(this::handleEvent)
                        .build();

        executor.execute(engine);

        log.info("Debezium Started");
    }

    private void handleEvent(
            ChangeEvent<String, String> event
    ) {

        try {

            if (event.value() == null) {
                return;
            }

            JsonNode root =
                    objectMapper.readTree(event.value());

            JsonNode after = root.get("after");

            if (after == null) {
                return;
            }

            String topic =
                    after.get("topic").asText();

            String payload =
                    after.get("payload").asText();

            OrderMessage order =
                    objectMapper.readValue(
                            payload,
                            OrderMessage.class
                    );

            jmsTemplate.convertAndSend(
                    topic,
                    order
            );

            log.info(
                    "Published to MQ {}",
                    order.getOrderId()
            );

        } catch (Exception e) {

            log.error(
                    "Debezium failed {}",
                    e.getMessage()
            );
        }
    }

    @PreDestroy
    public void stop() throws Exception {

        if (engine != null) {
            engine.close();
        }

        if (executor != null) {
            executor.shutdown();
        }
    }
}

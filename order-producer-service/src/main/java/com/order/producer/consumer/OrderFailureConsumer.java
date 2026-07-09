package com.order.producer.consumer;

import com.order.producer.model.OrderMessage;
import com.order.producer.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderFailureConsumer {

    @Autowired
    private OrderRepository orderRepository;

    @JmsListener(destination = "DEV.ORDER.FAILED")
    public void handleFailure(OrderMessage order) {

        log.info("Received failure for order {}", order.getOrderId());

        orderRepository.findById(order.getOrderId())
                .ifPresent(o -> {
                    o.setStatus("CANCELLED");
                    orderRepository.save(o);

                    log.info("Order {} marked CANCELLED",
                            order.getOrderId());
                });
    }
}
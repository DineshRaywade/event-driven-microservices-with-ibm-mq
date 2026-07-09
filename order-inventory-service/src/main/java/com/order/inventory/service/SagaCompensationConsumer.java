package com.order.inventory.service;

import com.order.inventory.entity.CompensationLogEntity;
import com.order.inventory.entity.InventoryEntity;
import com.order.inventory.model.OrderMessage;
import com.order.inventory.repository.CompensationLogRepository;
import com.order.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
@Slf4j
public class SagaCompensationConsumer {

    @Autowired
    private InventoryRepository repository;

    @Autowired
    private CompensationLogRepository compensationLogRepository;

    @JmsListener(destination = "DEV.ORDER.FAILED")
    public void compensate(OrderMessage order) {

        repository.findByOrderId(order.getOrderId())
                .ifPresent(inv -> {

                    inv.setStatus("RELEASED");
                    repository.save(inv);

                    CompensationLogEntity logEntity = new CompensationLogEntity();

                    logEntity.setOrderId(order.getOrderId());
                    logEntity.setAction("INVENTORY_RELEASED");
                    logEntity.setCreatedAt(LocalDateTime.now());

                    compensationLogRepository.save(logEntity);

                    log.info(
                            "Inventory Released {}",
                            order.getOrderId()
                    );
                });
    }
}
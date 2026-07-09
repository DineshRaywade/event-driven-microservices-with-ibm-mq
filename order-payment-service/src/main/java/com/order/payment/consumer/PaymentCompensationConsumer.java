package com.order.payment.consumer;

import com.order.payment.entity.CompensationLogEntity;
import com.order.payment.model.OrderMessage;
import com.order.payment.repository.CompensationLogRepository;
import com.order.payment.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class PaymentCompensationConsumer {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private CompensationLogRepository compensationLogRepository;

    @JmsListener(destination = "DEV.ORDER.FAILED")
    public void compensate(OrderMessage order) {

        paymentRepository
                .findByOrderId(order.getOrderId())
                .ifPresent(payment -> {

                    payment.setStatus("CANCELLED");

                    paymentRepository.save(payment);

                    CompensationLogEntity logEntity =
                            new CompensationLogEntity();

                    logEntity.setOrderId(order.getOrderId());
                    logEntity.setAction("PAYMENT_CANCELLED");
                    logEntity.setCreatedAt(LocalDateTime.now());

                    compensationLogRepository.save(logEntity);

                    log.info(
                            "Payment cancelled {}",
                            order.getOrderId()
                    );
                });
    }
}
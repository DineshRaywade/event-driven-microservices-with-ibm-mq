package com.order.payment.repository;

import com.order.payment.entity.CompensationLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompensationLogRepository extends JpaRepository<CompensationLogEntity, Long> {
}

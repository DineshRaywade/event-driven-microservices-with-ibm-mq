package com.order.inventory.repository;

import com.order.inventory.entity.CompensationLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompensationLogRepository extends JpaRepository<CompensationLogEntity, Long> {
}

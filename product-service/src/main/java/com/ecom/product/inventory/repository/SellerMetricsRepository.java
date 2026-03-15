package com.ecom.product.inventory.repository;

import com.ecom.product.inventory.model.SellerMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SellerMetricsRepository extends JpaRepository<SellerMetrics, Long> {
}

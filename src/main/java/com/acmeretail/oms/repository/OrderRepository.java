package com.acmeretail.oms.repository;

import com.acmeretail.oms.domain.enums.OrderStatus;
import com.acmeretail.oms.domain.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByCustomerId(Long customerId);

    List<Order> findByStatus(OrderStatus status);
}

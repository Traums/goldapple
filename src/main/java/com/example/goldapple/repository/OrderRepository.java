package com.example.goldapple.repository;

import com.example.goldapple.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerIdOrderByIdAsc(Long customerId);

    List<Order> findAllByOrderByIdDesc();
}

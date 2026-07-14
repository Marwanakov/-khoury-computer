package com.khourycomputer.application.repository;

import com.khourycomputer.domain.enums.OrderStatus;
import com.khourycomputer.domain.model.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    List<Order> findAll();

    Optional<Order> findById(Long id);

    List<Order> findByUserId(Long userId);

    List<Order> findByStatus(OrderStatus status);

    Order save(Order order);

    void deleteById(Long id);
}
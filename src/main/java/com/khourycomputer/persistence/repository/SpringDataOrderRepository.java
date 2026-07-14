package com.khourycomputer.persistence.repository;

import com.khourycomputer.domain.enums.OrderStatus;
import com.khourycomputer.persistence.entity.OrderEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SpringDataOrderRepository extends CrudRepository<OrderEntity, Long> {

    List<OrderEntity> findByUserId(Long userId);

    List<OrderEntity> findByStatus(OrderStatus status);
}
package com.khourycomputer.persistence.repository;

import com.khourycomputer.application.repository.OrderRepository;
import com.khourycomputer.domain.enums.OrderStatus;
import com.khourycomputer.domain.model.Order;
import com.khourycomputer.persistence.mapper.OrderMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Repository
public class OrderRepositoryImpl implements OrderRepository {

    private final SpringDataOrderRepository springDataOrderRepository;
    private final OrderMapper orderMapper;

    public OrderRepositoryImpl(
            SpringDataOrderRepository springDataOrderRepository,
            OrderMapper orderMapper
    ) {
        this.springDataOrderRepository = springDataOrderRepository;
        this.orderMapper = orderMapper;
    }

    @Override
    public List<Order> findAll() {
        return StreamSupport.stream(springDataOrderRepository.findAll().spliterator(), false)
                .map(orderMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Order> findById(Long id) {
        return springDataOrderRepository.findById(id)
                .map(orderMapper::toDomain);
    }

    @Override
    public List<Order> findByUserId(Long userId) {
        return springDataOrderRepository.findByUserId(userId)
                .stream()
                .map(orderMapper::toDomain)
                .toList();
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return springDataOrderRepository.findByStatus(status)
                .stream()
                .map(orderMapper::toDomain)
                .toList();
    }

    @Override
    public Order save(Order order) {
        return orderMapper.toDomain(
                springDataOrderRepository.save(orderMapper.toEntity(order))
        );
    }

    @Override
    public void deleteById(Long id) {
        springDataOrderRepository.deleteById(id);
    }
}
package com.khourycomputer.persistence.repository;

import com.khourycomputer.persistence.entity.CartEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface SpringDataCartRepository extends CrudRepository<CartEntity, Long> {

    Optional<CartEntity> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
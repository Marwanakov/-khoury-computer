package com.khourycomputer.application.repository;

import com.khourycomputer.domain.model.Cart;

import java.util.Optional;

public interface CartRepository {

    Optional<Cart> findById(Long id);

    Optional<Cart> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    Cart save(Cart cart);

    void deleteById(Long id);

    void deleteByUserId(Long userId);
}
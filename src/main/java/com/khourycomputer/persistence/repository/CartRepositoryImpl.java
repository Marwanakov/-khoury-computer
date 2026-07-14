package com.khourycomputer.persistence.repository;

import com.khourycomputer.application.repository.CartRepository;
import com.khourycomputer.domain.model.Cart;
import com.khourycomputer.persistence.mapper.CartMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CartRepositoryImpl implements CartRepository {

    private final SpringDataCartRepository springDataCartRepository;
    private final CartMapper cartMapper;

    public CartRepositoryImpl(
            SpringDataCartRepository springDataCartRepository,
            CartMapper cartMapper
    ) {
        this.springDataCartRepository = springDataCartRepository;
        this.cartMapper = cartMapper;
    }

    @Override
    public Optional<Cart> findById(Long id) {
        return springDataCartRepository.findById(id)
                .map(cartMapper::toDomain);
    }

    @Override
    public Optional<Cart> findByUserId(Long userId) {
        return springDataCartRepository.findByUserId(userId)
                .map(cartMapper::toDomain);
    }

    @Override
    public boolean existsByUserId(Long userId) {
        return springDataCartRepository.existsByUserId(userId);
    }

    @Override
    public Cart save(Cart cart) {
        return cartMapper.toDomain(
                springDataCartRepository.save(cartMapper.toEntity(cart))
        );
    }

    @Override
    public void deleteById(Long id) {
        springDataCartRepository.deleteById(id);
    }

    // Spring Data does not provide deleteByUserId by default here.
    // So we first find the cart that belongs to the user,
    // then delete it using the real cart ID.
    @Override
    public void deleteByUserId(Long userId) {
        springDataCartRepository.findByUserId(userId)
                .ifPresent(cart -> springDataCartRepository.deleteById(cart.id()));
    }
}
package com.khourycomputer.persistence.mapper;

import com.khourycomputer.domain.model.Cart;
import com.khourycomputer.domain.model.CartItem;
import com.khourycomputer.persistence.entity.CartEntity;
import com.khourycomputer.persistence.entity.CartItemEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CartMapper {

    public Cart toDomain(CartEntity cartEntity) {
        if (cartEntity == null) {
            return null;
        }

        return new Cart(
                cartEntity.id(),
                cartEntity.userId(),
                mapItemsToDomain(cartEntity.items())
        );
    }

    public CartEntity toEntity(Cart cart) {
        if (cart == null) {
            return null;
        }

        return new CartEntity(
                cart.getId(),
                cart.getUserId(),
                mapItemsToEntity(cart.getItems())
        );
    }

    private List<CartItem> mapItemsToDomain(List<CartItemEntity> items) {
        if (items == null) {
            return List.of();
        }

        return items.stream()
                .map(this::mapItemToDomain)
                .toList();
    }

    private List<CartItemEntity> mapItemsToEntity(List<CartItem> items) {
        if (items == null) {
            return List.of();
        }

        return items.stream()
                .map(this::mapItemToEntity)
                .toList();
    }

    // CartItem does not have its own mapper because it belongs inside the Cart aggregate.
    // These helper methods map one single cart item between domain and persistence.
    private CartItem mapItemToDomain(CartItemEntity itemEntity) {
        return new CartItem(
                itemEntity.id(),
                itemEntity.productId(),
                itemEntity.productName(),
                itemEntity.unitPrice(),
                itemEntity.quantity()
        );
    }

    private CartItemEntity mapItemToEntity(CartItem item) {
        return new CartItemEntity(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                item.getUnitPrice(),
                item.getQuantity()
        );
    }
}
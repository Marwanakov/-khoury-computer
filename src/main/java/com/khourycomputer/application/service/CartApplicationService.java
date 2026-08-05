package com.khourycomputer.application.service;

import com.khourycomputer.application.dto.cart.AddCartItemRequest;
import com.khourycomputer.application.dto.cart.CartItemResponse;
import com.khourycomputer.application.dto.cart.CartResponse;
import com.khourycomputer.application.dto.cart.UpdateCartItemQuantityRequest;
import com.khourycomputer.application.repository.CartRepository;
import com.khourycomputer.application.repository.ProductRepository;
import com.khourycomputer.application.repository.UserRepository;
import com.khourycomputer.domain.model.Cart;
import com.khourycomputer.domain.model.CartItem;
import com.khourycomputer.domain.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartApplicationService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartApplicationService(
            CartRepository cartRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    // User story: customer views his cart to check products, quantities, and total
    // price.
    @Transactional
    public CartResponse getCartByUserId(Long userId) {
        validateUserExists(userId);

        Cart cart = getOrCreateCart(userId);

        return toResponse(cart);
    }

    // User story: customer adds products to cart.
    @Transactional
    public CartResponse addItemToCart(Long userId, AddCartItemRequest request) {
        validateUserExists(userId);
        validateQuantity(request.quantity());

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));

        validateStock(product, request.quantity());

        Cart cart = getOrCreateCart(userId);

        List<CartItem> updatedItems = addOrIncreaseItem(cart.getItems(), product, request.quantity());

        Cart updatedCart = new Cart(
                cart.getId(),
                cart.getUserId(),
                updatedItems);

        Cart savedCart = cartRepository.save(updatedCart);

        return toResponse(savedCart);
    }

    // User story: customer changes product quantity before ordering.
    @Transactional
    public CartResponse updateItemQuantity(Long userId, UpdateCartItemQuantityRequest request) {
        validateUserExists(userId);
        validateQuantity(request.quantity());

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));

        validateStock(product, request.quantity());

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found."));

        List<CartItem> updatedItems = cart.getItems()
                .stream()
                .map(item -> item.getProductId().equals(request.productId())
                        ? new CartItem(
                                item.getId(),
                                item.getProductId(),
                                item.getProductName(),
                                item.getUnitPrice(),
                                request.quantity())
                        : item)
                .toList();

        Cart updatedCart = new Cart(
                cart.getId(),
                cart.getUserId(),
                updatedItems);

        Cart savedCart = cartRepository.save(updatedCart);

        return toResponse(savedCart);
    }

    // User story: customer removes products from cart before submitting the order.
    @Transactional
    public CartResponse removeProductFromCart(Long userId, Long productId) {
        validateUserExists(userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found."));

        List<CartItem> updatedItems = cart.getItems()
                .stream()
                .filter(item -> !item.getProductId().equals(productId))
                .toList();

        Cart updatedCart = new Cart(
                cart.getId(),
                cart.getUserId(),
                updatedItems);

        Cart savedCart = cartRepository.save(updatedCart);

        return toResponse(savedCart);
    }

    @Transactional
    public void clearCart(Long userId) {
        validateUserExists(userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found."));

        Cart emptyCart = new Cart(
                cart.getId(),
                cart.getUserId(),
                List.of());

        cartRepository.save(emptyCart);
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(new Cart(null, userId, List.of())));
    }

    private List<CartItem> addOrIncreaseItem(List<CartItem> existingItems, Product product, int quantityToAdd) {
        List<CartItem> updatedItems = new ArrayList<>();
        boolean productAlreadyInCart = false;

        for (CartItem item : existingItems) {
            if (item.getProductId().equals(product.getId())) {
                int newQuantity = item.getQuantity() + quantityToAdd;

                validateStock(product, newQuantity);

                updatedItems.add(new CartItem(
                        item.getId(),
                        item.getProductId(),
                        item.getProductName(),
                        item.getUnitPrice(),
                        newQuantity));

                productAlreadyInCart = true;
            } else {
                updatedItems.add(item);
            }
        }

        if (!productAlreadyInCart) {
            updatedItems.add(new CartItem(
                    null,
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    quantityToAdd));
        }

        return updatedItems;
    }

    private void validateUserExists(Long userId) {
        if (userId == null || !userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found.");
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
    }

    private void validateStock(Product product, int quantity) {
        if (quantity > product.getStockQuantity()) {
            throw new IllegalArgumentException("Requested quantity is greater than available stock.");
        }
    }

    private CartResponse toResponse(Cart cart) {
        return new CartResponse(
                cart.getId(),
                cart.getUserId(),
                cart.getItems()
                        .stream()
                        .map(this::toItemResponse)
                        .toList(),
                cart.getTotalPrice());
    }

    private CartItemResponse toItemResponse(CartItem item) {
        return new CartItemResponse(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getSubtotal());
    }
}
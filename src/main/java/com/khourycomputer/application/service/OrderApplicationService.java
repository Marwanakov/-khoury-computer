package com.khourycomputer.application.service;

import com.khourycomputer.domain.exception.OrderNotFoundException;
import com.khourycomputer.domain.exception.ProductNotFoundException;
import com.khourycomputer.application.dto.common.address.AddressResponse;
import com.khourycomputer.application.dto.order.OrderItemResponse;
import com.khourycomputer.application.dto.order.OrderResponse;
import com.khourycomputer.application.dto.order.SubmitOrderResponse;
import com.khourycomputer.application.repository.CartRepository;
import com.khourycomputer.application.repository.OrderRepository;
import com.khourycomputer.application.repository.ProductRepository;
import com.khourycomputer.application.repository.UserRepository;
import com.khourycomputer.domain.enums.OrderStatus;
import com.khourycomputer.domain.model.Address;
import com.khourycomputer.domain.model.Cart;
import com.khourycomputer.domain.model.CustomerInfo;
import com.khourycomputer.domain.model.Order;
import com.khourycomputer.domain.model.OrderItem;
import com.khourycomputer.domain.model.Product;
import com.khourycomputer.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.khourycomputer.application.exception.CartPriceChangedException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderApplicationService {

        private final OrderRepository orderRepository;
        private final CartRepository cartRepository;
        private final UserRepository userRepository;
        private final ProductRepository productRepository;
        private final ProductPricingService productPricingService;

        public OrderApplicationService(
                        OrderRepository orderRepository,
                        CartRepository cartRepository,
                        UserRepository userRepository,
                        ProductRepository productRepository,
                        ProductPricingService productPricingService) {
                this.orderRepository = orderRepository;

                this.cartRepository = cartRepository;

                this.userRepository = userRepository;

                this.productRepository = productRepository;

                this.productPricingService = productPricingService;
        }

        // User story: customer submits an order request so the store can contact him
        // and confirm it.
        @Transactional
        public SubmitOrderResponse submitOrder(Long userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "User not found."));

                Cart cart = cartRepository.findByUserId(userId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Cart not found."));

                if (cart.isEmpty()) {
                        throw new IllegalArgumentException(
                                        "Cannot submit an empty cart.");
                }

                List<OrderItem> validatedOrderItems = createValidatedOrderItems(cart);

                Order order = new Order(
                                null,
                                user.getId(),
                                createCustomerInfoSnapshot(user),
                                validatedOrderItems,
                                OrderStatus.PENDING,
                                LocalDateTime.now(),
                                null);

                Order savedOrder = orderRepository.save(order);

                clearCart(cart);

                return new SubmitOrderResponse(
                                toResponse(savedOrder),
                                "Your order request was submitted successfully. "
                                                + "The store will contact you soon to confirm it.");
        }

        @Transactional(readOnly = true)
        public OrderResponse getOrderById(Long orderId) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(OrderNotFoundException::new);

                return toResponse(order);
        }

        @Transactional(readOnly = true)
        public OrderResponse getOrderByIdForUser(Long userId, Long orderId) {
                Order order = findOrderOwnedByUser(userId, orderId);

                return toResponse(order);
        }

        @Transactional(readOnly = true)
        public List<OrderResponse> listOrders() {
                return orderRepository.findAll()
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        // User story: customer can see his own order history.
        @Transactional(readOnly = true)
        public List<OrderResponse> listOrdersByUserId(Long userId) {
                if (!userRepository.existsById(userId)) {
                        throw new IllegalArgumentException("User not found.");
                }

                return orderRepository.findByUserId(userId)
                                .stream()
                                .sorted(
                                                Comparator.comparing(Order::getCreatedAt)
                                                                .reversed())
                                .map(this::toResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<OrderResponse> listOrdersByStatus(OrderStatus status) {
                if (status == null) {
                        throw new IllegalArgumentException("Order status cannot be null.");
                }

                return orderRepository.findByStatus(status)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Transactional
        public OrderResponse applyCustomDiscount(
                        Long orderId,
                        BigDecimal agreedFinalTotal) {

                Order order = orderRepository.findById(orderId)
                                .orElseThrow(OrderNotFoundException::new);

                order.applyAgreedFinalTotal(
                                agreedFinalTotal,
                                LocalDateTime.now());

                Order savedOrder = orderRepository.save(order);

                return toResponse(savedOrder);
        }

        @Transactional
        public OrderResponse removeCustomDiscount(
                        Long orderId) {

                Order order = orderRepository.findById(orderId)
                                .orElseThrow(OrderNotFoundException::new);

                order.removeCustomDiscount();

                Order savedOrder = orderRepository.save(order);

                return toResponse(savedOrder);
        }

        @Transactional
        public OrderResponse confirmOrder(Long orderId) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(OrderNotFoundException::new);

                // Validate the status before changing inventory.
                order.confirm();

                reduceStockForOrder(order);

                return toResponse(orderRepository.save(order));
        }

        @Transactional
        public OrderResponse completeOrder(Long orderId) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(OrderNotFoundException::new);

                order.complete();

                return toResponse(orderRepository.save(order));
        }

        @Transactional
        public OrderResponse cancelOrderByCustomer(Long userId, Long orderId) {
                Order order = findOrderOwnedByUser(userId, orderId);

                if (order.getStatus() != OrderStatus.PENDING) {
                        throw new IllegalStateException(
                                        "Only pending orders can be cancelled by the customer.");
                }

                order.cancel();

                return toResponse(orderRepository.save(order));
        }

        @Transactional
        public OrderResponse cancelOrder(Long orderId) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(OrderNotFoundException::new);

                OrderStatus previousStatus = order.getStatus();

                // This also rejects repeated or invalid cancellation.
                order.cancel();

                if (previousStatus == OrderStatus.CONFIRMED) {
                        restoreStockForOrder(order);
                }

                return toResponse(orderRepository.save(order));
        }

        private CustomerInfo createCustomerInfoSnapshot(User user) {
                return new CustomerInfo(
                                user.getFullName(),
                                user.getEmail(),
                                user.getPhoneNumber(),
                                user.getAddress());
        }

        private void clearCart(Cart cart) {
                Cart emptyCart = new Cart(
                                cart.getId(),
                                cart.getUserId(),
                                List.of());

                cartRepository.save(emptyCart);
        }

        private Order findOrderOwnedByUser(Long userId, Long orderId) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(OrderNotFoundException::new);

                if (!order.getUserId().equals(userId)) {
                        throw new OrderNotFoundException();
                }

                return order;
        }

        private void reduceStockForOrder(Order order) {
                for (OrderItem item : order.getItems()) {
                        Product product = getProductForStockUpdate(
                                        item.getProductId());

                        product.reduceStock(item.getQuantity());
                        productRepository.save(product);
                }
        }

        private void restoreStockForOrder(Order order) {
                for (OrderItem item : order.getItems()) {
                        Product product = getProductForStockUpdate(
                                        item.getProductId());

                        product.restoreStock(item.getQuantity());
                        productRepository.save(product);
                }
        }

        private Product getProductForStockUpdate(Long productId) {
                return productRepository.findById(productId)
                                .orElseThrow(() -> new ProductNotFoundException(productId));
        }

        private List<OrderItem> createValidatedOrderItems(
                        Cart cart) {
                List<OrderItem> orderItems = new ArrayList<>();

                for (var cartItem : cart.getItems()) {
                        Product product = productRepository.findById(
                                        cartItem.getProductId())
                                        .orElseThrow(() -> new ProductNotFoundException(
                                                        cartItem.getProductId()));

                        validateCurrentStock(
                                        product,
                                        cartItem.getQuantity());

                        BigDecimal currentUnitPrice = productPricingService
                                        .getEffectiveUnitPrice(product);

                        validateCartPriceIsCurrent(
                                        product,
                                        cartItem.getUnitPrice(),
                                        currentUnitPrice);

                        orderItems.add(
                                        new OrderItem(
                                                        null,
                                                        product.getId(),
                                                        product.getName(),
                                                        currentUnitPrice,
                                                        cartItem.getQuantity()));
                }

                return orderItems;
        }

        private void validateCartPriceIsCurrent(
                        Product product,
                        BigDecimal cartUnitPrice,
                        BigDecimal currentUnitPrice) {
                if (cartUnitPrice.compareTo(
                                currentUnitPrice) == 0) {
                        return;
                }

                throw new CartPriceChangedException(
                                product.getName(),
                                cartUnitPrice,
                                currentUnitPrice);
        }

        private void validateCurrentStock(
                        Product product,
                        int requestedQuantity) {
                int availableStock = product.getStockQuantity();

                if (requestedQuantity <= availableStock) {
                        return;
                }

                if (availableStock == 0) {
                        throw new IllegalArgumentException(
                                        product.getName()
                                                        + " is currently sold out. "
                                                        + "Remove it from your cart to continue.");
                }

                if (availableStock == 1) {
                        throw new IllegalArgumentException(
                                        "Only 1 unit of "
                                                        + product.getName()
                                                        + " is currently available. "
                                                        + "Reduce the cart quantity to continue.");
                }

                throw new IllegalArgumentException(
                                "Only "
                                                + availableStock
                                                + " units of "
                                                + product.getName()
                                                + " are currently available. "
                                                + "Reduce the cart quantity to continue.");
        }

        private OrderResponse toResponse(Order order) {
                CustomerInfo customerInfo = order.getCustomerInfo();

                return new OrderResponse(
                                order.getId(),
                                order.getUserId(),
                                customerInfo.getName(),
                                customerInfo.getEmail(),
                                customerInfo.getPhoneNumber(),
                                toAddressResponse(customerInfo.getAddress()),
                                order.getItems()
                                                .stream()
                                                .map(this::toItemResponse)
                                                .toList(),
                                order.getStatus(),
                                order.getCreatedAt(),
                                order.getSubtotal(),
                                order.getCustomDiscountAmount(),
                                order.getCustomDiscountAppliedAt(),
                                order.getTotalPrice());
        }

        private OrderItemResponse toItemResponse(OrderItem item) {
                return new OrderItemResponse(
                                item.getId(),
                                item.getProductId(),
                                item.getProductName(),
                                findCurrentProductImageUrl(item.getProductId()),
                                item.getUnitPrice(),
                                item.getQuantity(),
                                item.getSubtotal());
        }

        private AddressResponse toAddressResponse(Address address) {
                return new AddressResponse(
                                address.getCity(),
                                address.getStreet(),
                                address.getDetails());
        }

        private String findCurrentProductImageUrl(Long productId) {
                return productRepository.findById(productId)
                                .map(Product::getImageUrl)
                                .orElse("");
        }
}
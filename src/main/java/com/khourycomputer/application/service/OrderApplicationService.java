package com.khourycomputer.application.service;

import com.khourycomputer.application.dto.common.address.AddressResponse;
import com.khourycomputer.application.dto.order.OrderItemResponse;
import com.khourycomputer.application.dto.order.OrderResponse;
import com.khourycomputer.application.dto.order.SubmitOrderResponse;
import com.khourycomputer.application.repository.CartRepository;
import com.khourycomputer.application.repository.OrderRepository;
import com.khourycomputer.application.repository.UserRepository;
import com.khourycomputer.domain.enums.OrderStatus;
import com.khourycomputer.domain.model.Address;
import com.khourycomputer.domain.model.Cart;
import com.khourycomputer.domain.model.CartItem;
import com.khourycomputer.domain.model.CustomerInfo;
import com.khourycomputer.domain.model.Order;
import com.khourycomputer.domain.model.OrderItem;
import com.khourycomputer.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderApplicationService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    public OrderApplicationService(
            OrderRepository orderRepository,
            CartRepository cartRepository,
            UserRepository userRepository
    ) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
    }

    // User story: customer submits an order request so the store can contact him and confirm it.
    @Transactional
    public SubmitOrderResponse submitOrder(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found."));

        if (cart.isEmpty()) {
            throw new IllegalArgumentException("Cannot submit an empty cart.");
        }

        Order order = new Order(
                null,
                user.getId(),
                createCustomerInfoSnapshot(user),
                createOrderItemsFromCart(cart),
                OrderStatus.PENDING,
                LocalDateTime.now()
        );

        Order savedOrder = orderRepository.save(order);

        clearCart(cart);

        return new SubmitOrderResponse(
                toResponse(savedOrder),
                "Your order request was submitted successfully. The store will contact you soon to confirm it."
        );
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found."));

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
    public OrderResponse confirmOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found."));

        order.confirm();

        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse completeOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found."));

        order.complete();

        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found."));

        order.cancel();

        return toResponse(orderRepository.save(order));
    }

    private CustomerInfo createCustomerInfoSnapshot(User user) {
        return new CustomerInfo(
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getAddress()
        );
    }

    private List<OrderItem> createOrderItemsFromCart(Cart cart) {
        return cart.getItems()
                .stream()
                .map(this::createOrderItemFromCartItem)
                .toList();
    }

    private OrderItem createOrderItemFromCartItem(CartItem cartItem) {
        return new OrderItem(
                null,
                cartItem.getProductId(),
                cartItem.getProductName(),
                cartItem.getUnitPrice(),
                cartItem.getQuantity()
        );
    }

    private void clearCart(Cart cart) {
        Cart emptyCart = new Cart(
                cart.getId(),
                cart.getUserId(),
                List.of()
        );

        cartRepository.save(emptyCart);
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
                order.getTotalPrice()
        );
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getSubtotal()
        );
    }

    private AddressResponse toAddressResponse(Address address) {
        return new AddressResponse(
                address.getCity(),
                address.getStreet(),
                address.getDetails()
        );
    }
}
package com.khourycomputer.application.dto.order;

import com.khourycomputer.application.dto.common.address.AddressResponse;
import com.khourycomputer.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long userId,
        String customerName,
        String customerEmail,
        String customerPhoneNumber,
        AddressResponse deliveryAddress,
        List<OrderItemResponse> items,
        OrderStatus status,
        LocalDateTime createdAt,
        BigDecimal totalPrice
) {
}
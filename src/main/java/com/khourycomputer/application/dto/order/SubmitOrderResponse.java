package com.khourycomputer.application.dto.order;

public record SubmitOrderResponse(
        OrderResponse order,
        String confirmationMessage
) {
}
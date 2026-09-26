package com.khourycomputer.application.dto.order;

import com.khourycomputer.application.dto.common.address.AddressRequest;

public record SubmitOrderRequest(
        String phoneCountryCode,
        String phoneNumber,
        AddressRequest deliveryAddress
) {
}
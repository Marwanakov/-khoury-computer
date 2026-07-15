package com.khourycomputer.application.dto.common.address;

public record AddressRequest(
        String city,
        String street,
        String details
) {
}
package com.khourycomputer.application.dto.user;

import com.khourycomputer.application.dto.common.address.AddressRequest;

public record RegisterUserRequest(
        String firstName,
        String lastName,
        String email,
        String password,
        String phoneNumber,
        AddressRequest address
) {
}
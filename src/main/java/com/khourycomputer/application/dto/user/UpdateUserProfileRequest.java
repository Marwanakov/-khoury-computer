package com.khourycomputer.application.dto.user;

import com.khourycomputer.application.dto.common.address.AddressRequest;

public record UpdateUserProfileRequest(
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        AddressRequest address
) {
}
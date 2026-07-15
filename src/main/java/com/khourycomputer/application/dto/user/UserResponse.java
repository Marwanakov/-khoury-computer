package com.khourycomputer.application.dto.user;

import com.khourycomputer.application.dto.common.address.AddressResponse;
import com.khourycomputer.domain.enums.UserRole;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String fullName,
        String email,
        String phoneNumber,
        AddressResponse address,
        UserRole role
) {
}
package com.khourycomputer.application.dto.storecontact;

import com.khourycomputer.application.dto.common.address.AddressRequest;

public record UpdateStoreContactInfoRequest(
        String phoneNumber,
        String facebookMessengerLink,
        String whatsappNumber,
        String email,
        AddressRequest storeAddress
) {
}
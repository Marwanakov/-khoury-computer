package com.khourycomputer.application.dto.storecontact;

import com.khourycomputer.application.dto.common.address.AddressResponse;

public record StoreContactInfoResponse(
        Long id,
        String phoneNumber,
        String facebookMessengerLink,
        String whatsappNumber,
        String email,
        AddressResponse storeAddress
) {
}
package com.khourycomputer.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

@Table("store_contact_info")
public record StoreContactInfoEntity(
        @Id Long id,

        @Column("phone_number")
        String phoneNumber,

        @Column("facebook_messenger_link")
        String facebookMessengerLink,

        @Column("whatsapp_number")
        String whatsappNumber,

        String email,

        @Embedded.Nullable(prefix = "store_address_")
        AddressEntity storeAddress
) {
}
package com.khourycomputer.persistence.entity;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;

public record CustomerInfoEntity(
        String name,

        String email,

        @Column("phone_number")
        String phoneNumber,

        @Embedded.Nullable(prefix = "address_")
        AddressEntity address
) {}
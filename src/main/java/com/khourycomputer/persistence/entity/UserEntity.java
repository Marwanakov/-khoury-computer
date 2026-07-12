package com.khourycomputer.persistence.entity;

import com.khourycomputer.domain.enums.UserRole;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

@Table("users")
public record UserEntity(
        @Id Long id,

        @Column("first_name")
        String firstName,

        @Column("last_name")
        String lastName,

        String email,

        @Column("password_hash")
        String passwordHash,

        @Column("phone_number")
        String phoneNumber,

        // Put the AddressEntity fields inside the users table with address_ prefix.
        @Embedded.Nullable(prefix = "address_") 
        AddressEntity address,

        UserRole role
) {}
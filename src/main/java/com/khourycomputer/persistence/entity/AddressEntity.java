package com.khourycomputer.persistence.entity;

public record AddressEntity(
        String city,
        String street,
        String details
) {}
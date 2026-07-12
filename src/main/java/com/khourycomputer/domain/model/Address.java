// The Address class is part of the business/domain. It describes a customer’s delivery address.
// It belongs to the value objects.

package com.khourycomputer.domain.model;

import java.util.Objects;

public class Address {

    private String city;
    private String street;
    private String details;

    public Address(String city, String street, String details) {
        setCity(city);
        setStreet(street);
        setDetails(details);
    }

    public String getCity() {
        return city;
    }

    public String getStreet() {
        return street;
    }

    public String getDetails() {
        return details;
    }

    public String getFullAddress() {
        return street + ", " + city + " - " + details;
    }

    private void setCity(String city) {
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City cannot be empty.");
        }

        this.city = city.trim();
    }

    private void setStreet(String street) {
        if (street == null || street.isBlank()) {
            throw new IllegalArgumentException("Street cannot be empty.");
        }

        this.street = street.trim();
    }

    private void setDetails(String details) {
        this.details = Objects.requireNonNullElse(details, "").trim();
    }
}
// It belongs to the value objects.

package com.khourycomputer.domain.model;

public class CustomerInfo {

    private String name;
    private String email;
    private String phoneNumber;
    private Address address;

    public CustomerInfo(String name, String email, String phoneNumber, Address address) {
        setName(name);
        setEmail(email);
        setPhoneNumber(phoneNumber);
        setAddress(address);
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Address getAddress() {
        return address;
    }

    private void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Customer name cannot be empty.");
        }

        this.name = name.trim();
    }

    private void setEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Customer email cannot be empty.");
        }

        if (!email.contains("@")) {
            throw new IllegalArgumentException("Customer email must be valid.");
        }

        this.email = email.trim().toLowerCase();
    }

    private void setPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("Customer phone number cannot be empty.");
        }

        this.phoneNumber = phoneNumber.trim();
    }

    private void setAddress(Address address) {
        if (address == null) {
            throw new IllegalArgumentException("Customer address cannot be null.");
        }

        this.address = address;
    }
}
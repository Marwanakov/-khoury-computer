package com.khourycomputer.domain.model;

import java.util.Objects;

public class StoreContactInfo {

    private Long id;
    private String phoneNumber;
    private String facebookMessengerLink;
    private String whatsappNumber;
    private String email;
    private Address storeAddress;

    public StoreContactInfo(
            Long id,
            String phoneNumber,
            String facebookMessengerLink,
            String whatsappNumber,
            String email,
            Address storeAddress
    ) {
        setId(id);
        setPhoneNumber(phoneNumber);
        setFacebookMessengerLink(facebookMessengerLink);
        setWhatsappNumber(whatsappNumber);
        setEmail(email);
        setStoreAddress(storeAddress);
    }

    public Long getId() {
        return id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getFacebookMessengerLink() {
        return facebookMessengerLink;
    }

    public String getWhatsappNumber() {
        return whatsappNumber;
    }

    public String getEmail() {
        return email;
    }

    public Address getStoreAddress() {
        return storeAddress;
    }

    public void updateContactInfo(
            String phoneNumber,
            String facebookMessengerLink,
            String whatsappNumber,
            String email,
            Address storeAddress
    ) {
        setPhoneNumber(phoneNumber);
        setFacebookMessengerLink(facebookMessengerLink);
        setWhatsappNumber(whatsappNumber);
        setEmail(email);
        setStoreAddress(storeAddress);
    }

    private void setId(Long id) {
        this.id = id;
    }

    private void setPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be empty.");
        }

        this.phoneNumber = phoneNumber.trim();
    }

    private void setFacebookMessengerLink(String facebookMessengerLink) {
        this.facebookMessengerLink = Objects.requireNonNullElse(facebookMessengerLink, "").trim();
    }

    private void setWhatsappNumber(String whatsappNumber) {
        this.whatsappNumber = Objects.requireNonNullElse(whatsappNumber, "").trim();
    }

    private void setEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty.");
        }

        if (!email.contains("@")) {
            throw new IllegalArgumentException("Email must be valid.");
        }

        this.email = email.trim().toLowerCase();
    }

    private void setStoreAddress(Address storeAddress) {
        if (storeAddress == null) {
            throw new IllegalArgumentException("Store address cannot be null.");
        }

        this.storeAddress = storeAddress;
    }
}
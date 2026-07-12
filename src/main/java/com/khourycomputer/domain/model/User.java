package com.khourycomputer.domain.model;

import com.khourycomputer.domain.enums.UserRole;

import java.util.Objects;

public class User {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String passwordHash;
    private String phoneNumber;
    private Address address;
    private UserRole role;

    public User(
            Long id,
            String firstName,
            String lastName,
            String email,
            String passwordHash,
            String phoneNumber,
            Address address,
            UserRole role
    ) {
        setId(id);
        setFirstName(firstName);
        setLastName(lastName);
        setEmail(email);
        setPasswordHash(passwordHash);
        setPhoneNumber(phoneNumber);
        setAddress(address);
        setRole(role);
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Address getAddress() {
        return address;
    }

    public UserRole getRole() {
        return role;
    }

    public void changeName(String firstName, String lastName) {
        setFirstName(firstName);
        setLastName(lastName);
    }

    public void changeContactInfo(String email, String phoneNumber, Address address) {
        setEmail(email);
        setPhoneNumber(phoneNumber);
        setAddress(address);
}

    public void changePasswordHash(String passwordHash) {
        setPasswordHash(passwordHash);
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    private void setId(Long id) {
        this.id = id;
    }

    private void setFirstName(String firstName) {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name cannot be empty.");
        }

        this.firstName = firstName.trim();
    }

    private void setLastName(String lastName) {
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be empty.");
        }

        this.lastName = lastName.trim();
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

    private void setPasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("Password hash cannot be empty.");
        }

        this.passwordHash = passwordHash;
    }

    private void setPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be empty.");
        }

        this.phoneNumber = phoneNumber.trim();
    }

    private void setAddress(Address address) {
         if (address == null) {
        throw new IllegalArgumentException("Address cannot be null.");
    }

        this.address = address;
    }

    private void setRole(UserRole role) {
        this.role = Objects.requireNonNullElse(role, UserRole.CUSTOMER);
    }
}
package com.khourycomputer.domain.model;

import com.khourycomputer.domain.enums.UserAuthProvider;
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
    private UserAuthProvider authProvider;
    private String providerSubject;

    /*
     * Temporary compatibility constructor for existing local account creation.
     * It allows the application to keep compiling while Google authentication
     * is introduced in stages.
     */
    public User(
            Long id,
            String firstName,
            String lastName,
            String email,
            String passwordHash,
            String phoneNumber,
            Address address,
            UserRole role) {
        this(
                id,
                firstName,
                lastName,
                email,
                passwordHash,
                phoneNumber,
                address,
                role,
                UserAuthProvider.LOCAL,
                null);
    }

    public User(
            Long id,
            String firstName,
            String lastName,
            String email,
            String passwordHash,
            String phoneNumber,
            Address address,
            UserRole role,
            UserAuthProvider authProvider,
            String providerSubject) {
        setId(id);
        setFirstName(firstName);
        setLastName(lastName);
        setEmail(email);
        setRole(role);
        setAuthentication(
                passwordHash,
                authProvider,
                providerSubject);
        setPhoneNumber(phoneNumber);
        setAddress(address);
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

    public UserAuthProvider getAuthProvider() {
        return authProvider;
    }

    public String getProviderSubject() {
        return providerSubject;
    }

    public void changeName(
            String firstName,
            String lastName) {
        setFirstName(firstName);
        setLastName(lastName);
    }

    public void changeContactInfo(
            String email,
            String phoneNumber,
            Address address) {
        setEmail(email);
        setPhoneNumber(phoneNumber);
        setAddress(address);
    }

    public void changePasswordHash(String passwordHash) {
        if (authProvider != UserAuthProvider.LOCAL) {
            throw new IllegalStateException(
                    "Google accounts do not use local passwords.");
        }

        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException(
                    "Password hash cannot be empty.");
        }

        this.passwordHash = passwordHash;
    }

    public void connectGoogleIdentity(String providerSubject) {
        if (providerSubject == null || providerSubject.isBlank()) {
            throw new IllegalArgumentException(
                    "Google account identifier cannot be empty.");
        }

        this.authProvider = UserAuthProvider.GOOGLE;
        this.providerSubject = providerSubject.trim();
        this.passwordHash = null;
    }

    public void synchronizeExternalIdentity(
            String firstName,
            String lastName,
            String email) {
        if (authProvider != UserAuthProvider.GOOGLE) {
            throw new IllegalStateException(
                    "Only Google accounts can synchronize an external identity.");
        }

        setFirstName(firstName);
        setLastName(lastName);
        setEmail(email);
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    private void setId(Long id) {
        this.id = id;
    }

    private void setFirstName(String firstName) {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException(
                    "First name cannot be empty.");
        }

        this.firstName = firstName.trim();
    }

    private void setLastName(String lastName) {
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException(
                    "Last name cannot be empty.");
        }

        this.lastName = lastName.trim();
    }

    private void setEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email cannot be empty.");
        }

        if (!email.contains("@")) {
            throw new IllegalArgumentException(
                    "Email must be valid.");
        }

        this.email = email.trim().toLowerCase();
    }

    private void setPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            this.phoneNumber = null;
            return;
        }

        this.phoneNumber = phoneNumber.trim();
    }

    private void setAddress(Address address) {
        this.address = address;
    }

    private void setRole(UserRole role) {
        this.role = Objects.requireNonNullElse(
                role,
                UserRole.CUSTOMER);
    }

    private void setAuthentication(
            String passwordHash,
            UserAuthProvider authProvider,
            String providerSubject) {
        UserAuthProvider resolvedProvider = Objects.requireNonNullElse(
                authProvider,
                UserAuthProvider.LOCAL);

        if (resolvedProvider == UserAuthProvider.LOCAL) {
            if (passwordHash == null || passwordHash.isBlank()) {
                throw new IllegalArgumentException(
                        "Local accounts require a password hash.");
            }

            this.authProvider = UserAuthProvider.LOCAL;
            this.passwordHash = passwordHash;
            this.providerSubject = null;
            return;
        }

        if (providerSubject == null || providerSubject.isBlank()) {
            throw new IllegalArgumentException(
                    "Google accounts require an account identifier.");
        }

        this.authProvider = UserAuthProvider.GOOGLE;
        this.passwordHash = null;
        this.providerSubject = providerSubject.trim();
    }
}
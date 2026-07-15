package com.khourycomputer.application.service;

import com.khourycomputer.application.dto.common.address.AddressRequest;
import com.khourycomputer.application.dto.common.address.AddressResponse;
import com.khourycomputer.application.dto.user.RegisterUserRequest;
import com.khourycomputer.application.dto.user.UpdateUserProfileRequest;
import com.khourycomputer.application.dto.user.UserResponse;
import com.khourycomputer.application.repository.UserRepository;
import com.khourycomputer.domain.enums.UserRole;
import com.khourycomputer.domain.model.Address;
import com.khourycomputer.domain.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserApplicationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserApplicationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // User story: customer creates an account with name, email, phone number, password, and address.
    @Transactional
    public UserResponse registerUser(RegisterUserRequest request) {
        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists.");
        }

        User user = new User(
                null,
                request.firstName(),
                request.lastName(),
                email,
                encodePassword(request.password()),
                request.phoneNumber(),
                toAddress(request.address()),
                UserRole.CUSTOMER
        );

        User savedUser = userRepository.save(user);

        return toResponse(savedUser);
    }

    // User story: customer edits profile information so his data stays correct.
    @Transactional
    public UserResponse updateUserProfile(Long userId, UpdateUserProfileRequest request) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        String newEmail = normalizeEmail(request.email());

        userRepository.findByEmail(newEmail)
                .filter(userWithSameEmail -> !userWithSameEmail.getId().equals(userId))
                .ifPresent(userWithSameEmail -> {
                    throw new IllegalArgumentException("Email already exists.");
                });

        User updatedUser = new User(
                existingUser.getId(),
                request.firstName(),
                request.lastName(),
                newEmail,
                existingUser.getPasswordHash(),
                request.phoneNumber(),
                toAddress(request.address()),
                existingUser.getRole()
        );

        User savedUser = userRepository.save(updatedUser);

        return toResponse(savedUser);
    }

    // User story: customer can view his personal information.
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        return toResponse(user);
    }

    // User story support: login later needs to load a user by email.
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        return toResponse(user);
    }

    // Admin/support use case: list all users.
    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found.");
        }

        userRepository.deleteById(userId);
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty.");
        }

        return email.trim().toLowerCase();
    }

    private String encodePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }

        return passwordEncoder.encode(password);
    }

    private Address toAddress(AddressRequest addressRequest) {
        if (addressRequest == null) {
            throw new IllegalArgumentException("Address cannot be empty.");
        }

        return new Address(
                addressRequest.city(),
                addressRequest.street(),
                addressRequest.details()
        );
    }

    private AddressResponse toAddressResponse(Address address) {
        return new AddressResponse(
                address.getCity(),
                address.getStreet(),
                address.getDetails()
        );
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                toAddressResponse(user.getAddress()),
                user.getRole()
        );
    }
}
package com.khourycomputer.application.service;

import com.khourycomputer.application.dto.common.address.AddressRequest;
import com.khourycomputer.application.dto.common.address.AddressResponse;
import com.khourycomputer.application.dto.user.UpdateUserProfileRequest;
import com.khourycomputer.application.dto.user.UserResponse;
import com.khourycomputer.application.repository.UserRepository;
import com.khourycomputer.domain.enums.UserRole;
import com.khourycomputer.domain.exception.CustomerNotFoundException;
import com.khourycomputer.domain.model.Address;
import com.khourycomputer.domain.model.User;
import com.khourycomputer.domain.model.PalestinianPhoneNumber;
import com.khourycomputer.application.dto.user.ExternalAuthenticationRequest;
import com.khourycomputer.domain.enums.UserAuthProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserApplicationService {

        private final UserRepository userRepository;

        public UserApplicationService(
                        UserRepository userRepository) {
                this.userRepository = userRepository;
        }

        @Transactional
        public UserResponse authenticateExternalUser(
                        ExternalAuthenticationRequest request) {
                validateExternalAuthenticationRequest(request);

                String providerSubject = request.providerSubject().trim();

                String email = normalizeEmail(request.email());

                String firstName = normalizeExternalName(
                                request.firstName(),
                                "Google");

                String lastName = normalizeExternalName(
                                request.lastName(),
                                "User");

                User userByExternalIdentity = userRepository
                                .findByAuthProviderAndProviderSubject(
                                                request.authProvider(),
                                                providerSubject)
                                .orElse(null);

                if (userByExternalIdentity != null) {
                        ensureEmailAvailableForUser(
                                        email,
                                        userByExternalIdentity.getId());

                        userByExternalIdentity.synchronizeExternalIdentity(
                                        firstName,
                                        lastName,
                                        email);

                        return toResponse(
                                        userRepository.save(userByExternalIdentity));
                }

                User userByEmail = userRepository.findByEmail(email)
                                .orElse(null);

                if (userByEmail != null) {
                        if (userByEmail.getAuthProvider() == UserAuthProvider.GOOGLE) {
                                throw new IllegalArgumentException(
                                                "This email is already connected to another Google account.");
                        }

                        userByEmail.connectGoogleIdentity(providerSubject);

                        userByEmail.synchronizeExternalIdentity(
                                        firstName,
                                        lastName,
                                        email);

                        return toResponse(
                                        userRepository.save(userByEmail));
                }

                User newCustomer = new User(
                                null,
                                firstName,
                                lastName,
                                email,
                                null,
                                null,
                                null,
                                UserRole.CUSTOMER,
                                request.authProvider(),
                                providerSubject);

                return toResponse(
                                userRepository.save(newCustomer));
        }

        // User story: customer edits profile information so his data stays correct.
        @Transactional
        public UserResponse updateUserProfile(
                        Long userId,
                        UpdateUserProfileRequest request) {
                User existingUser = userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "User not found."));

                String newEmail = normalizeEmail(request.email());

                userRepository.findByEmail(newEmail)
                                .filter(userWithSameEmail -> !userWithSameEmail.getId().equals(userId))
                                .ifPresent(userWithSameEmail -> {
                                        throw new IllegalArgumentException(
                                                        "Email already exists.");
                                });

                PalestinianPhoneNumber phoneNumber = PalestinianPhoneNumber.fromParts(
                                request.phoneCountryCode(),
                                request.phoneNumber());

                User updatedUser = new User(
                                existingUser.getId(),
                                request.firstName(),
                                request.lastName(),
                                newEmail,
                                existingUser.getPasswordHash(),
                                phoneNumber.getInternationalNumber(),
                                toAddress(request.address()),
                                existingUser.getRole());

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

        @Transactional(readOnly = true)
        public UserResponse getCustomerById(Long customerId) {
                User customer = userRepository.findById(customerId)
                                .orElseThrow(() -> new CustomerNotFoundException(customerId));

                if (customer.getRole() != UserRole.CUSTOMER) {
                        throw new CustomerNotFoundException(customerId);
                }

                return toResponse(customer);
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

        private Address toAddress(AddressRequest addressRequest) {
                if (addressRequest == null) {
                        throw new IllegalArgumentException("Address cannot be empty.");
                }

                return new Address(
                                addressRequest.city(),
                                addressRequest.street(),
                                addressRequest.details());
        }

        private AddressResponse toAddressResponse(Address address) {
                if (address == null) {
                        return null;
                }

                return new AddressResponse(
                                address.getCity(),
                                address.getStreet(),
                                address.getDetails());
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
                                user.getRole());
        }

        private void validateExternalAuthenticationRequest(
                        ExternalAuthenticationRequest request) {
                if (request == null) {
                        throw new IllegalArgumentException(
                                        "External authentication request cannot be empty.");
                }

                if (request.authProvider() != UserAuthProvider.GOOGLE) {
                        throw new IllegalArgumentException(
                                        "Unsupported authentication provider.");
                }

                if (request.providerSubject() == null
                                || request.providerSubject().isBlank()) {
                        throw new IllegalArgumentException(
                                        "Google account identifier cannot be empty.");
                }

                if (!request.emailVerified()) {
                        throw new IllegalArgumentException(
                                        "Google must verify the email address.");
                }
        }

        private void ensureEmailAvailableForUser(
                        String email,
                        Long currentUserId) {
                userRepository.findByEmail(email)
                                .filter(existingUser -> !existingUser.getId().equals(currentUserId))
                                .ifPresent(existingUser -> {
                                        throw new IllegalArgumentException(
                                                        "This email is already used by another account.");
                                });
        }

        private String normalizeExternalName(
                        String name,
                        String fallback) {
                if (name == null || name.isBlank()) {
                        return fallback;
                }

                return name.trim();
        }
}
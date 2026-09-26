package com.khourycomputer.application.dto.user;

import com.khourycomputer.domain.enums.UserAuthProvider;

public record ExternalAuthenticationRequest(
        UserAuthProvider authProvider,
        String providerSubject,
        String email,
        boolean emailVerified,
        String firstName,
        String lastName
) {
}
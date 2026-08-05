package com.khourycomputer.config.security;

import com.khourycomputer.application.dto.user.UserResponse;
import com.khourycomputer.application.service.UserApplicationService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserApplicationService userApplicationService;

    public CurrentUserService(
            UserApplicationService userApplicationService
    ) {
        this.userApplicationService = userApplicationService;
    }

    public UserResponse getCurrentUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {

            throw new IllegalStateException(
                    "No authenticated user is available."
            );
        }

        return userApplicationService.getUserByEmail(
                authentication.getName()
        );
    }

    public Long getCurrentUserId() {
        return getCurrentUser().id();
    }
}
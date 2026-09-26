package com.khourycomputer.config.security;

import com.khourycomputer.application.dto.user.ExternalAuthenticationRequest;
import com.khourycomputer.application.dto.user.UserResponse;
import com.khourycomputer.application.service.UserApplicationService;
import com.khourycomputer.domain.enums.UserAuthProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class GoogleOidcUserService
        implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final OidcUserService delegate;
    private final UserApplicationService userApplicationService;

    public GoogleOidcUserService(
            UserApplicationService userApplicationService
    ) {
        this.delegate = new OidcUserService();
        this.userApplicationService = userApplicationService;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest)
            throws OAuth2AuthenticationException {

        OidcUser googleUser = delegate.loadUser(userRequest);

        UserResponse authenticatedUser;

        try {
            authenticatedUser =
                    userApplicationService.authenticateExternalUser(
                            new ExternalAuthenticationRequest(
                                    UserAuthProvider.GOOGLE,
                                    googleUser.getSubject(),
                                    googleUser.getEmail(),
                                    Boolean.TRUE.equals(
                                            googleUser.getEmailVerified()
                                    ),
                                    googleUser.getGivenName(),
                                    googleUser.getFamilyName()
                            )
                    );
        } catch (RuntimeException exception) {
            OAuth2Error error = new OAuth2Error(
                    "google_authentication_failed",
                    "Google authentication failed.",
                    null
            );

            throw new OAuth2AuthenticationException(
                    error,
                    exception.getMessage(),
                    exception
            );
        }

        Collection<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(
                        "ROLE_" + authenticatedUser.role().name()
                )
        );

        if (googleUser.getUserInfo() == null) {
            return new DefaultOidcUser(
                    authorities,
                    googleUser.getIdToken(),
                    "email"
            );
        }

        return new DefaultOidcUser(
                authorities,
                googleUser.getIdToken(),
                googleUser.getUserInfo(),
                "email"
        );
    }
}
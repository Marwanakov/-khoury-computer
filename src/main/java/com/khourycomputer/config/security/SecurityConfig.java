package com.khourycomputer.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

@Configuration
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http,
                        PendingCartAuthenticationSuccessHandler successHandler,
                        GoogleOidcUserService googleOidcUserService) throws Exception {

                XorCsrfTokenRequestAttributeHandler csrfRequestHandler = new XorCsrfTokenRequestAttributeHandler();

                csrfRequestHandler.setCsrfRequestAttributeName(null);

                return http
                                .csrf(csrf -> csrf
                                                .csrfTokenRequestHandler(
                                                                csrfRequestHandler))

                                .authorizeHttpRequests(auth -> auth

                                                // Public pages, authentication routes,
                                                // and static resources.
                                                .requestMatchers(
                                                                "/",
                                                                "/products/**",
                                                                "/deals",
                                                                "/new-arrivals",
                                                                "/best-sellers",
                                                                "/contact",
                                                                "/login",
                                                                "/oauth2/**",
                                                                "/login/oauth2/**",
                                                                "/access-denied",
                                                                "/error",
                                                                "/favicon.ico",
                                                                "/css/**",
                                                                "/js/**",
                                                                "/images/**",
                                                                "/uploads/**",
                                                                "/oauth2/**",
                                                                "/login/oauth2/**")
                                                .permitAll()

                                                // Admin area.
                                                .requestMatchers("/admin/**")
                                                .hasRole("ADMIN")

                                                // Guests may start the pending-cart flow.
                                                // Customers may add products normally.
                                                // Admins are denied.
                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/cart/items")
                                                .access((authentication, context) -> {
                                                        boolean isAnonymous = authentication.get()
                                                                        .getAuthorities()
                                                                        .stream()
                                                                        .anyMatch(authority -> authority
                                                                                        .getAuthority()
                                                                                        .equals(
                                                                                                        "ROLE_ANONYMOUS"));

                                                        boolean isCustomer = authentication.get()
                                                                        .getAuthorities()
                                                                        .stream()
                                                                        .anyMatch(authority -> authority
                                                                                        .getAuthority()
                                                                                        .equals(
                                                                                                        "ROLE_CUSTOMER"));

                                                        return new AuthorizationDecision(
                                                                        isAnonymous || isCustomer);
                                                })

                                                // Customer-only pages.
                                                .requestMatchers(
                                                                "/cart/**",
                                                                "/orders/**",
                                                                "/profile/**")
                                                .hasRole("CUSTOMER")

                                                .anyRequest()
                                                .authenticated())

                                // Google authentication is used for customers.
                                .oauth2Login(oauth2Login -> oauth2Login
                                                .loginPage("/login")
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .oidcUserService(googleOidcUserService))
                                                .successHandler(successHandler)
                                                .failureUrl("/login?oauthError"))

                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login?logout")
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID")
                                                .permitAll())

                                .exceptionHandling(exceptionHandling -> exceptionHandling
                                                .authenticationEntryPoint(
                                                                new LoginUrlAuthenticationEntryPoint(
                                                                                "/login"))
                                                .accessDeniedPage("/access-denied"))

                                .httpBasic(httpBasic -> httpBasic.disable())

                                .build();
        }
}
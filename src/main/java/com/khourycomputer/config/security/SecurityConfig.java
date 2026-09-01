package com.khourycomputer.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;

@Configuration
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http,
                        PendingCartAuthenticationSuccessHandler successHandler) throws Exception {

                XorCsrfTokenRequestAttributeHandler csrfRequestHandler = new XorCsrfTokenRequestAttributeHandler();

                csrfRequestHandler.setCsrfRequestAttributeName(null);

                return http

                                .csrf(csrf -> csrf
                                                .csrfTokenRequestHandler(csrfRequestHandler))
                                .authorizeHttpRequests(auth -> auth

                                                // Public pages and resources
                                                .requestMatchers(
                                                                "/",
                                                                "/products/**",
                                                                "/deals",
                                                                "/new-arrivals",
                                                                "/contact",
                                                                "/register",
                                                                "/login",
                                                                "/access-denied",
                                                                "/error",
                                                                "/favicon.ico",
                                                                "/css/**",
                                                                "/js/**",
                                                                "/images/**",
                                                                "/uploads/**")
                                                .permitAll()

                                                // Admin area
                                                .requestMatchers("/admin/**")
                                                .hasRole("ADMIN")

                                                // Guests may begin the pending-cart login flow.
                                                // Customers may add products normally.
                                                // Admins are denied.
                                                .requestMatchers(HttpMethod.POST, "/cart/items")
                                                .access((authentication, context) -> {

                                                        boolean isAnonymous = authentication.get()
                                                                        .getAuthorities()
                                                                        .stream()
                                                                        .anyMatch(authority -> authority.getAuthority()
                                                                                        .equals("ROLE_ANONYMOUS"));

                                                        boolean isCustomer = authentication.get()
                                                                        .getAuthorities()
                                                                        .stream()
                                                                        .anyMatch(authority -> authority.getAuthority()
                                                                                        .equals("ROLE_CUSTOMER"));

                                                        return new AuthorizationDecision(
                                                                        isAnonymous || isCustomer);
                                                })

                                                // Customer-only account pages
                                                .requestMatchers(
                                                                "/cart/**",
                                                                "/orders/**",
                                                                "/profile/**")
                                                .hasRole("CUSTOMER")

                                                // Safe default for future routes
                                                .anyRequest()
                                                .authenticated())

                                // Spring Security handles POST /login
                                .formLogin(formLogin -> formLogin
                                                .loginPage("/login")
                                                .loginProcessingUrl("/login")
                                                .successHandler(successHandler)
                                                .failureUrl("/login?error")
                                                .permitAll())

                                // Spring Security handles POST /logout
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login?logout")
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID")
                                                .permitAll())

                                // Show a clean page when an authenticated user
                                // does not have permission to access a route.
                                .exceptionHandling(exceptionHandling -> exceptionHandling
                                                .accessDeniedPage("/access-denied"))

                                // Disable browser HTTP Basic authentication.
                                .httpBasic(httpBasic -> httpBasic.disable())

                                .build();
        }
}
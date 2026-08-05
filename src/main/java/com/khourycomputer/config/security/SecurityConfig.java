package com.khourycomputer.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http, PendingCartAuthenticationSuccessHandler successHandler)
                        throws Exception {

                return http
                                .authorizeHttpRequests(auth -> auth

                                                // Public pages and resources
                                                .requestMatchers(
                                                                "/",
                                                                "/products/**",
                                                                "/contact",
                                                                "/register",
                                                                "/login",
                                                                "/error",
                                                                "/favicon.ico",
                                                                "/css/**",
                                                                "/js/**",
                                                                "/images/**")
                                                .permitAll()

                                                // Future admin area
                                                .requestMatchers("/admin/**")
                                                .hasRole("ADMIN")

                                                .requestMatchers(HttpMethod.POST, "/cart/items")
                                                .permitAll()

                                                // Customer account pages
                                                .requestMatchers(
                                                                "/cart/**",
                                                                "/orders/**",
                                                                "/profile/**")
                                                .authenticated()

                                                // Safe default for future routes
                                                .anyRequest().authenticated())

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

                                // We do not want browser HTTP Basic authentication.
                                .httpBasic(httpBasic -> httpBasic.disable())

                                .build();
        }
}
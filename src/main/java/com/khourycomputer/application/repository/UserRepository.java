package com.khourycomputer.application.repository;

import com.khourycomputer.domain.enums.UserAuthProvider;
import com.khourycomputer.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    List<User> findAll();

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    Optional<User> findByAuthProviderAndProviderSubject(
            UserAuthProvider authProvider,
            String providerSubject
    );

    boolean existsById(Long id);

    boolean existsByEmail(String email);

    User save(User user);

    void deleteById(Long id);
}
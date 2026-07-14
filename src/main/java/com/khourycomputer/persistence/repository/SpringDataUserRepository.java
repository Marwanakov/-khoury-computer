package com.khourycomputer.persistence.repository;

import com.khourycomputer.persistence.entity.UserEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface SpringDataUserRepository extends CrudRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
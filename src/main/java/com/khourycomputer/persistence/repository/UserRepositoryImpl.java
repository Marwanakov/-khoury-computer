package com.khourycomputer.persistence.repository;

import com.khourycomputer.application.repository.UserRepository;
import com.khourycomputer.domain.model.User;
import com.khourycomputer.persistence.mapper.UserMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;
    private final UserMapper userMapper;

    public UserRepositoryImpl(
            SpringDataUserRepository springDataUserRepository,
            UserMapper userMapper
    ) {
        this.springDataUserRepository = springDataUserRepository;
        this.userMapper = userMapper;
    }

    @Override
    public List<User> findAll() {
        return StreamSupport.stream(springDataUserRepository.findAll().spliterator(), false)
                .map(userMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<User> findById(Long id) {
        return springDataUserRepository.findById(id)
                .map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springDataUserRepository.findByEmail(email)
                .map(userMapper::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        return springDataUserRepository.existsById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataUserRepository.existsByEmail(email);
    }

    @Override
    public User save(User user) {
        return userMapper.toDomain(
                springDataUserRepository.save(userMapper.toEntity(user))
        );
    }

    @Override
    public void deleteById(Long id) {
        springDataUserRepository.deleteById(id);
    }
}
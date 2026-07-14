package com.khourycomputer.persistence.mapper;

import com.khourycomputer.domain.model.User;
import com.khourycomputer.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    private final AddressMapper addressMapper;

    public UserMapper(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    public User toDomain(UserEntity userEntity) {
        if (userEntity == null) {
            return null;
        }

        return new User(
                userEntity.id(),
                userEntity.firstName(),
                userEntity.lastName(),
                userEntity.email(),
                userEntity.passwordHash(),
                userEntity.phoneNumber(),
                addressMapper.toDomain(userEntity.address()),
                userEntity.role()
        );
    }

    public UserEntity toEntity(User user) {
        if (user == null) {
            return null;
        }

        return new UserEntity(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getPhoneNumber(),
                addressMapper.toEntity(user.getAddress()),
                user.getRole()
        );
    }
}
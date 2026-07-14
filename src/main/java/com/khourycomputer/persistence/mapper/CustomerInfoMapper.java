package com.khourycomputer.persistence.mapper;

import com.khourycomputer.domain.model.CustomerInfo;
import com.khourycomputer.persistence.entity.CustomerInfoEntity;
import org.springframework.stereotype.Component;

@Component
public class CustomerInfoMapper {

    private final AddressMapper addressMapper;

    public CustomerInfoMapper(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    public CustomerInfo toDomain(CustomerInfoEntity customerInfoEntity) {
        if (customerInfoEntity == null) {
            return null;
        }

        return new CustomerInfo(
                customerInfoEntity.name(),
                customerInfoEntity.email(),
                customerInfoEntity.phoneNumber(),
                addressMapper.toDomain(customerInfoEntity.address())
        );
    }

    public CustomerInfoEntity toEntity(CustomerInfo customerInfo) {
        if (customerInfo == null) {
            return null;
        }

        return new CustomerInfoEntity(
                customerInfo.getName(),
                customerInfo.getEmail(),
                customerInfo.getPhoneNumber(),
                addressMapper.toEntity(customerInfo.getAddress())
        );
    }
}
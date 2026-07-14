package com.khourycomputer.persistence.mapper;

import com.khourycomputer.domain.model.Address;
import com.khourycomputer.persistence.entity.AddressEntity;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {

    public Address toDomain(AddressEntity addressEntity) {
        if (addressEntity == null) {
            return null;
        }

        return new Address(
                addressEntity.city(),
                addressEntity.street(),
                addressEntity.details()
        );
    }

    public AddressEntity toEntity(Address address) {
        if (address == null) {
            return null;
        }

        return new AddressEntity(
                address.getCity(),
                address.getStreet(),
                address.getDetails()
        );
    }
}
package com.khourycomputer.persistence.mapper;

import com.khourycomputer.domain.model.StoreContactInfo;
import com.khourycomputer.persistence.entity.StoreContactInfoEntity;
import org.springframework.stereotype.Component;

@Component
public class StoreContactInfoMapper {

    private final AddressMapper addressMapper;

    public StoreContactInfoMapper(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    public StoreContactInfo toDomain(StoreContactInfoEntity storeContactInfoEntity) {
        if (storeContactInfoEntity == null) {
            return null;
        }

        return new StoreContactInfo(
                storeContactInfoEntity.id(),
                storeContactInfoEntity.phoneNumber(),
                storeContactInfoEntity.facebookMessengerLink(),
                storeContactInfoEntity.whatsappNumber(),
                storeContactInfoEntity.email(),
                addressMapper.toDomain(storeContactInfoEntity.storeAddress())
        );
    }

    public StoreContactInfoEntity toEntity(StoreContactInfo storeContactInfo) {
        if (storeContactInfo == null) {
            return null;
        }

        return new StoreContactInfoEntity(
                storeContactInfo.getId(),
                storeContactInfo.getPhoneNumber(),
                storeContactInfo.getFacebookMessengerLink(),
                storeContactInfo.getWhatsappNumber(),
                storeContactInfo.getEmail(),
                addressMapper.toEntity(storeContactInfo.getStoreAddress())
        );
    }
}
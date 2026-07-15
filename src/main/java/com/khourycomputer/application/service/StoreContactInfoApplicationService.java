package com.khourycomputer.application.service;

import com.khourycomputer.application.dto.common.address.AddressRequest;
import com.khourycomputer.application.dto.common.address.AddressResponse;
import com.khourycomputer.application.dto.storecontact.StoreContactInfoResponse;
import com.khourycomputer.application.dto.storecontact.UpdateStoreContactInfoRequest;
import com.khourycomputer.application.repository.StoreContactInfoRepository;
import com.khourycomputer.domain.model.Address;
import com.khourycomputer.domain.model.StoreContactInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StoreContactInfoApplicationService {

    private static final Long MAIN_STORE_CONTACT_INFO_ID = 1L;

    private final StoreContactInfoRepository storeContactInfoRepository;

    public StoreContactInfoApplicationService(StoreContactInfoRepository storeContactInfoRepository) {
        this.storeContactInfoRepository = storeContactInfoRepository;
    }

    // User story: customer can view the store contact info to contact the shop.
    @Transactional(readOnly = true)
    public StoreContactInfoResponse getStoreContactInfo() {
        StoreContactInfo storeContactInfo = storeContactInfoRepository.findMainContactInfo()
                .orElseThrow(() -> new IllegalArgumentException("Store contact information not found."));

        return toResponse(storeContactInfo);
    }

    // Admin use case: update the public store contact info shown on the website.
    @Transactional
    public StoreContactInfoResponse updateStoreContactInfo(UpdateStoreContactInfoRequest request) {
        StoreContactInfo existingStoreContactInfo = storeContactInfoRepository.findMainContactInfo()
                .orElse(null);

        Long id = existingStoreContactInfo == null
                ? MAIN_STORE_CONTACT_INFO_ID
                : existingStoreContactInfo.getId();

        StoreContactInfo updatedStoreContactInfo = new StoreContactInfo(
                id,
                request.phoneNumber(),
                request.facebookMessengerLink(),
                request.whatsappNumber(),
                request.email(),
                toAddress(request.storeAddress())
        );

        StoreContactInfo savedStoreContactInfo = storeContactInfoRepository.save(updatedStoreContactInfo);

        return toResponse(savedStoreContactInfo);
    }

    private Address toAddress(AddressRequest addressRequest) {
        if (addressRequest == null) {
            throw new IllegalArgumentException("Store address cannot be empty.");
        }

        return new Address(
                addressRequest.city(),
                addressRequest.street(),
                addressRequest.details()
        );
    }

    private AddressResponse toAddressResponse(Address address) {
        return new AddressResponse(
                address.getCity(),
                address.getStreet(),
                address.getDetails()
        );
    }

    private StoreContactInfoResponse toResponse(StoreContactInfo storeContactInfo) {
        return new StoreContactInfoResponse(
                storeContactInfo.getId(),
                storeContactInfo.getPhoneNumber(),
                storeContactInfo.getFacebookMessengerLink(),
                storeContactInfo.getWhatsappNumber(),
                storeContactInfo.getEmail(),
                toAddressResponse(storeContactInfo.getStoreAddress())
        );
    }
}
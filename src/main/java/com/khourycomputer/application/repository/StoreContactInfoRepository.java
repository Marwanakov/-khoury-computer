package com.khourycomputer.application.repository;

import com.khourycomputer.domain.model.StoreContactInfo;

import java.util.Optional;

public interface StoreContactInfoRepository {

    Optional<StoreContactInfo> findById(Long id);

    Optional<StoreContactInfo> findMainContactInfo();

    StoreContactInfo save(StoreContactInfo storeContactInfo);

    void deleteById(Long id);
}
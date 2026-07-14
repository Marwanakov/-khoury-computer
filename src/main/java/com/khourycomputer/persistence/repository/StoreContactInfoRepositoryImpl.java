package com.khourycomputer.persistence.repository;

import com.khourycomputer.application.repository.StoreContactInfoRepository;
import com.khourycomputer.domain.model.StoreContactInfo;
import com.khourycomputer.persistence.mapper.StoreContactInfoMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.stream.StreamSupport;

@Repository
public class StoreContactInfoRepositoryImpl implements StoreContactInfoRepository {

    private final SpringDataStoreContactInfoRepository springDataStoreContactInfoRepository;
    private final StoreContactInfoMapper storeContactInfoMapper;

    public StoreContactInfoRepositoryImpl(
            SpringDataStoreContactInfoRepository springDataStoreContactInfoRepository,
            StoreContactInfoMapper storeContactInfoMapper
    ) {
        this.springDataStoreContactInfoRepository = springDataStoreContactInfoRepository;
        this.storeContactInfoMapper = storeContactInfoMapper;
    }

    @Override
    public Optional<StoreContactInfo> findById(Long id) {
        return springDataStoreContactInfoRepository.findById(id)
                .map(storeContactInfoMapper::toDomain);
    }

    // The store contact info table will usually contain only one main row.
    // We load all rows and return the first one as the main contact information.
    @Override
    public Optional<StoreContactInfo> findMainContactInfo() {
        return StreamSupport.stream(springDataStoreContactInfoRepository.findAll().spliterator(), false)
                .findFirst()
                .map(storeContactInfoMapper::toDomain);
    }

    @Override
    public StoreContactInfo save(StoreContactInfo storeContactInfo) {
        return storeContactInfoMapper.toDomain(
                springDataStoreContactInfoRepository.save(storeContactInfoMapper.toEntity(storeContactInfo))
        );
    }

    @Override
    public void deleteById(Long id) {
        springDataStoreContactInfoRepository.deleteById(id);
    }
}
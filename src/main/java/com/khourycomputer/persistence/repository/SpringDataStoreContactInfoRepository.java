package com.khourycomputer.persistence.repository;

import com.khourycomputer.persistence.entity.StoreContactInfoEntity;
import org.springframework.data.repository.CrudRepository;

public interface SpringDataStoreContactInfoRepository extends CrudRepository<StoreContactInfoEntity, Long> {
}
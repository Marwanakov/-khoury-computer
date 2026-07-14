package com.khourycomputer.persistence.repository;

import com.khourycomputer.persistence.entity.CategoryEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface SpringDataCategoryRepository extends CrudRepository<CategoryEntity, Long> {

    Optional<CategoryEntity> findByName(String name);

    boolean existsByName(String name);
}
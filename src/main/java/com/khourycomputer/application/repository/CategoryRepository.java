package com.khourycomputer.application.repository;

import com.khourycomputer.domain.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {

    List<Category> findAll();

    Optional<Category> findById(Long id);

    Optional<Category> findByName(String name);

    boolean existsById(Long id);

    boolean existsByName(String name);

    Category save(Category category);

    void deleteById(Long id);
}
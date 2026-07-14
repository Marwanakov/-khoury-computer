package com.khourycomputer.persistence.repository;

import com.khourycomputer.application.repository.CategoryRepository;
import com.khourycomputer.domain.model.Category;
import com.khourycomputer.persistence.mapper.CategoryMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Repository
public class CategoryRepositoryImpl implements CategoryRepository {

    private final SpringDataCategoryRepository springDataCategoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryRepositoryImpl(
            SpringDataCategoryRepository springDataCategoryRepository,
            CategoryMapper categoryMapper
    ) {
        this.springDataCategoryRepository = springDataCategoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public List<Category> findAll() {
        return StreamSupport.stream(springDataCategoryRepository.findAll().spliterator(), false)
                .map(categoryMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Category> findById(Long id) {
        return springDataCategoryRepository.findById(id)
                .map(categoryMapper::toDomain);
    }

    @Override
    public Optional<Category> findByName(String name) {
        return springDataCategoryRepository.findByName(name)
                .map(categoryMapper::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        return springDataCategoryRepository.existsById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return springDataCategoryRepository.existsByName(name);
    }

    @Override
    public Category save(Category category) {
        return categoryMapper.toDomain(
                springDataCategoryRepository.save(categoryMapper.toEntity(category))
        );
    }

    @Override
    public void deleteById(Long id) {
        springDataCategoryRepository.deleteById(id);
    }
}
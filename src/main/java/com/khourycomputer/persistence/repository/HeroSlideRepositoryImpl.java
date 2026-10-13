package com.khourycomputer.persistence.repository;

import com.khourycomputer.application.repository.HeroSlideRepository;
import com.khourycomputer.domain.model.HeroSlide;
import com.khourycomputer.persistence.mapper.HeroSlideMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class HeroSlideRepositoryImpl
        implements HeroSlideRepository {

    private final SpringDataHeroSlideRepository repository;
    private final HeroSlideMapper mapper;

    public HeroSlideRepositoryImpl(
            SpringDataHeroSlideRepository repository,
            HeroSlideMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<HeroSlide> findAll() {
        return repository
                .findAllByOrderByDisplayOrderAscIdAsc()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<HeroSlide> findActive() {
        return repository
                .findByActiveTrueOrderByDisplayOrderAscIdAsc()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<HeroSlide> findById(Long id) {
        return repository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public HeroSlide save(HeroSlide heroSlide) {
        return mapper.toDomain(
                repository.save(
                        mapper.toEntity(heroSlide)
                )
        );
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
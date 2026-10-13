package com.khourycomputer.application.repository;

import com.khourycomputer.domain.model.HeroSlide;

import java.util.List;
import java.util.Optional;

public interface HeroSlideRepository {

    List<HeroSlide> findAll();

    List<HeroSlide> findActive();

    Optional<HeroSlide> findById(Long id);

    HeroSlide save(HeroSlide heroSlide);

    void deleteById(Long id);
}
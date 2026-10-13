package com.khourycomputer.persistence.repository;

import com.khourycomputer.persistence.entity.HeroSlideEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SpringDataHeroSlideRepository
        extends CrudRepository<HeroSlideEntity, Long> {

    List<HeroSlideEntity>
            findAllByOrderByDisplayOrderAscIdAsc();

    List<HeroSlideEntity>
            findByActiveTrueOrderByDisplayOrderAscIdAsc();
}
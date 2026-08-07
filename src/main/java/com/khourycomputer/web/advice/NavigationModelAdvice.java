package com.khourycomputer.web.advice;

import com.khourycomputer.application.dto.category.CategoryResponse;
import com.khourycomputer.application.service.CategoryApplicationService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Comparator;
import java.util.List;

@ControllerAdvice
public class NavigationModelAdvice {

    private final CategoryApplicationService categoryApplicationService;

    public NavigationModelAdvice(
            CategoryApplicationService categoryApplicationService
    ) {
        this.categoryApplicationService = categoryApplicationService;
    }

    @ModelAttribute("navigationCategories")
    public List<CategoryResponse> navigationCategories() {
        return categoryApplicationService.listCategories()
                .stream()
                .sorted(
                        Comparator.comparing(
                                CategoryResponse::name,
                                String.CASE_INSENSITIVE_ORDER
                        )
                )
                .toList();
    }
}
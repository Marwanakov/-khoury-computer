package com.khourycomputer.web.controller;

import com.khourycomputer.application.dto.category.CategoryResponse;
import com.khourycomputer.application.service.CategoryApplicationService;
import com.khourycomputer.web.viewmodel.home.HomeBrandCatalog;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Comparator;
import java.util.List;

@Controller
public class HomeController {

        private final CategoryApplicationService categoryApplicationService;

        public HomeController(
                        CategoryApplicationService categoryApplicationService) {
                this.categoryApplicationService = categoryApplicationService;
        }

        @GetMapping("/")
        public String showHomePage(Model model) {
                List<CategoryResponse> categories = categoryApplicationService.listCategories()
                                .stream()
                                .sorted(
                                                Comparator.comparing(
                                                                CategoryResponse::name,
                                                                String.CASE_INSENSITIVE_ORDER))
                                .toList();

                model.addAttribute(
                                "categories",
                                categories);

                model.addAttribute(
                                "brands",
                                HomeBrandCatalog.getBrands());

                return "public/home";
        }
}
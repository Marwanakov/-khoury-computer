package com.khourycomputer.web.controller;

import com.khourycomputer.application.dto.category.CategoryResponse;
import com.khourycomputer.application.dto.product.ProductResponse;
import com.khourycomputer.application.service.CategoryApplicationService;
import com.khourycomputer.application.service.ProductApplicationService;
import com.khourycomputer.domain.enums.ProductAvailabilityStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class PublicProductController {

    private final ProductApplicationService productApplicationService;
    private final CategoryApplicationService categoryApplicationService;

    public PublicProductController(
            ProductApplicationService productApplicationService,
            CategoryApplicationService categoryApplicationService
    ) {
        this.productApplicationService = productApplicationService;
        this.categoryApplicationService = categoryApplicationService;
    }

    @GetMapping("/products")
    public String showProductsPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) ProductAvailabilityStatus status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            Model model
    ) {
        List<ProductResponse> products = productApplicationService.filterProducts(
                keyword,
                categoryId,
                brand,
                status,
                minPrice,
                maxPrice
        );

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryApplicationService.listCategories());
        model.addAttribute("availabilityStatuses", ProductAvailabilityStatus.values());

        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("brand", brand);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

        return "public/products";
    }

    @GetMapping("/products/{productId}")
    public String showProductDetailsPage(
            @PathVariable Long productId,
            Model model
    ) {
        ProductResponse product = productApplicationService.getProductById(productId);
        CategoryResponse category = categoryApplicationService.getCategoryById(product.categoryId());

        model.addAttribute("product", product);
        model.addAttribute("category", category);

        return "public/product-details";
    }
}
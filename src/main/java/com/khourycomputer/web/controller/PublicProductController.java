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

import com.khourycomputer.application.dto.deal.ProductDealResponse;
import com.khourycomputer.application.service.ProductDealApplicationService;
import com.khourycomputer.web.viewmodel.product.ProductCardViewModel;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class PublicProductController {

        private final ProductApplicationService productApplicationService;
        private final CategoryApplicationService categoryApplicationService;
        private final ProductDealApplicationService productDealApplicationService;

        public PublicProductController(
                        ProductApplicationService productApplicationService,
                        CategoryApplicationService categoryApplicationService,
                        ProductDealApplicationService productDealApplicationService) {
                this.productApplicationService = productApplicationService;
                this.categoryApplicationService = categoryApplicationService;
                this.productDealApplicationService = productDealApplicationService;
        }

        @GetMapping("/products")
        public String showProductsPage(
                        @RequestParam(required = false) String keyword,

                        @RequestParam(required = false) Long categoryId,

                        @RequestParam(required = false) String brand,

                        @RequestParam(required = false) ProductAvailabilityStatus status,

                        @RequestParam(required = false) BigDecimal minPrice,

                        @RequestParam(required = false) BigDecimal maxPrice,

                        @RequestParam(defaultValue = "false") boolean dealsOnly,

                        @RequestParam(defaultValue = "false") boolean newArrivalsOnly,

                        @RequestParam(defaultValue = "false") boolean bestSellersOnly,

                        Model model) {
                List<ProductResponse> filteredProducts = productApplicationService
                                .filterStorefrontProducts(
                                                keyword,
                                                categoryId,
                                                brand,
                                                status,
                                                minPrice,
                                                maxPrice,
                                                newArrivalsOnly,
                                                bestSellersOnly);

                Map<Long, ProductDealResponse> activeDealsByProductId = productDealApplicationService
                                .listActiveDeals()
                                .stream()
                                .collect(
                                                Collectors.toMap(
                                                                ProductDealResponse::productId,
                                                                Function.identity(),
                                                                (firstDeal, secondDeal) -> firstDeal));

                List<ProductCardViewModel> productCards = filteredProducts.stream()
                                .filter(product -> !dealsOnly
                                                || activeDealsByProductId
                                                                .containsKey(product.id()))
                                .map(product -> new ProductCardViewModel(
                                                product,
                                                activeDealsByProductId.get(
                                                                product.id())))
                                .toList();

                model.addAttribute(
                                "productCards",
                                productCards);

                model.addAttribute(
                                "categories",
                                categoryApplicationService.listCategories());

                model.addAttribute(
                                "availabilityStatuses",
                                ProductAvailabilityStatus.values());

                model.addAttribute(
                                "keyword",
                                keyword);

                model.addAttribute(
                                "selectedCategoryId",
                                categoryId);

                model.addAttribute(
                                "brand",
                                brand);

                model.addAttribute(
                                "selectedStatus",
                                status);

                model.addAttribute(
                                "minPrice",
                                minPrice);

                model.addAttribute(
                                "maxPrice",
                                maxPrice);

                model.addAttribute(
                                "dealsOnly",
                                dealsOnly);

                model.addAttribute(
                                "newArrivalsOnly",
                                newArrivalsOnly);

                model.addAttribute(
                                "bestSellersOnly",
                                bestSellersOnly);

                return "public/products";
        }

        @GetMapping("/products/{productId}")
        public String showProductDetailsPage(
                        @PathVariable Long productId,
                        Model model) {
                ProductResponse product = productApplicationService.getProductById(
                                productId);

                CategoryResponse category = categoryApplicationService.getCategoryById(
                                product.categoryId());

                ProductDealResponse activeDeal = productDealApplicationService
                                .findActiveDealByProductId(
                                                productId);

                model.addAttribute(
                                "product",
                                product);

                model.addAttribute(
                                "category",
                                category);

                model.addAttribute(
                                "activeDeal",
                                activeDeal);

                return "public/product-details";
        }
}
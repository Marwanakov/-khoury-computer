package com.khourycomputer.web.controller;

import com.khourycomputer.application.dto.category.CategoryResponse;
import com.khourycomputer.application.dto.deal.ProductDealResponse;
import com.khourycomputer.application.dto.product.ProductResponse;
import com.khourycomputer.application.service.CategoryApplicationService;
import com.khourycomputer.application.service.HeroSlideApplicationService;
import com.khourycomputer.application.service.ProductApplicationService;
import com.khourycomputer.application.service.ProductDealApplicationService;
import com.khourycomputer.web.viewmodel.home.HomeBrandCatalog;
import com.khourycomputer.web.viewmodel.product.ProductCardViewModel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private static final int
            MAXIMUM_COLLECTION_PRODUCTS = 15;

    private final CategoryApplicationService
            categoryApplicationService;

    private final HeroSlideApplicationService
            heroSlideApplicationService;

    private final ProductApplicationService
            productApplicationService;

    private final ProductDealApplicationService
            productDealApplicationService;

    public HomeController(
            CategoryApplicationService
                    categoryApplicationService,
            HeroSlideApplicationService
                    heroSlideApplicationService,
            ProductApplicationService
                    productApplicationService,
            ProductDealApplicationService
                    productDealApplicationService
    ) {
        this.categoryApplicationService =
                categoryApplicationService;

        this.heroSlideApplicationService =
                heroSlideApplicationService;

        this.productApplicationService =
                productApplicationService;

        this.productDealApplicationService =
                productDealApplicationService;
    }

    @GetMapping("/")
    public String showHomePage(Model model) {
        List<CategoryResponse> categories =
                categoryApplicationService
                        .listCategories()
                        .stream()
                        .sorted(
                                Comparator.comparing(
                                        CategoryResponse::name,
                                        String.CASE_INSENSITIVE_ORDER
                                )
                        )
                        .toList();

        List<ProductResponse> allProducts =
                productApplicationService
                        .listProducts();

        List<ProductResponse> featuredProducts =
                productApplicationService
                        .listFeaturedProducts();

        List<ProductDealResponse> activeDeals =
                productDealApplicationService
                        .listActiveDeals();

        Map<Long, ProductResponse> productsById =
                allProducts.stream()
                        .collect(
                                Collectors.toMap(
                                        ProductResponse::id,
                                        Function.identity()
                                )
                        );

        Map<Long, ProductDealResponse>
                activeDealsByProductId =
                activeDeals.stream()
                        .collect(
                                Collectors.toMap(
                                        ProductDealResponse::productId,
                                        Function.identity(),
                                        (firstDeal, secondDeal) ->
                                                firstDeal
                                )
                        );

        List<ProductCardViewModel> homeDeals =
                activeDeals.stream()
                        .map(deal -> {
                            ProductResponse product =
                                    productsById.get(
                                            deal.productId()
                                    );

                            if (product == null) {
                                return null;
                            }

                            return new ProductCardViewModel(
                                    product,
                                    deal
                            );
                        })
                        .filter(Objects::nonNull)
                        .limit(MAXIMUM_COLLECTION_PRODUCTS)
                        .toList();

        List<ProductCardViewModel> homeNewArrivals =
                allProducts.stream()
                        .filter(ProductResponse::newArrival)
                        .sorted(
                                Comparator.comparing(
                                        ProductResponse::
                                                newArrivalMarkedAt
                                ).reversed()
                        )
                        .limit(MAXIMUM_COLLECTION_PRODUCTS)
                        .map(product ->
                                new ProductCardViewModel(
                                        product,
                                        activeDealsByProductId.get(
                                                product.id()
                                        )
                                )
                        )
                        .toList();

        List<ProductCardViewModel> homeBestSellers =
                allProducts.stream()
                        .filter(ProductResponse::bestSeller)
                        .sorted(
                                Comparator.comparing(
                                        ProductResponse::
                                                bestSellerMarkedAt
                                ).reversed()
                        )
                        .limit(MAXIMUM_COLLECTION_PRODUCTS)
                        .map(product ->
                                new ProductCardViewModel(
                                        product,
                                        activeDealsByProductId.get(
                                                product.id()
                                        )
                                )
                        )
                        .toList();

        List<ProductCardViewModel> homeFeaturedProducts =
                featuredProducts.stream()
                        .limit(MAXIMUM_COLLECTION_PRODUCTS)
                        .map(product ->
                                new ProductCardViewModel(
                                        product,
                                        activeDealsByProductId.get(
                                                product.id()
                                        )
                                )
                        )
                        .toList();

        model.addAttribute(
                "categories",
                categories
        );

        model.addAttribute(
                "brands",
                HomeBrandCatalog.getBrands()
        );

        model.addAttribute(
                "heroSlides",
                heroSlideApplicationService
                        .listActiveSlides()
        );

        model.addAttribute(
                "homeDeals",
                homeDeals
        );

        model.addAttribute(
                "homeNewArrivals",
                homeNewArrivals
        );

        model.addAttribute(
                "homeBestSellers",
                homeBestSellers
        );

        model.addAttribute(
                "homeFeaturedProducts",
                homeFeaturedProducts
        );

        return "public/home";
    }
}
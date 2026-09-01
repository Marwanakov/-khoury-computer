package com.khourycomputer.web.controller;

import com.khourycomputer.application.dto.deal.ProductDealResponse;
import com.khourycomputer.application.dto.product.ProductResponse;
import com.khourycomputer.application.service.ProductApplicationService;
import com.khourycomputer.application.service.ProductDealApplicationService;
import com.khourycomputer.web.viewmodel.product.ProductCardViewModel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class NewArrivalController {

    private final ProductApplicationService
            productApplicationService;

    private final ProductDealApplicationService
            productDealApplicationService;

    public NewArrivalController(
            ProductApplicationService
                    productApplicationService,
            ProductDealApplicationService
                    productDealApplicationService
    ) {
        this.productApplicationService =
                productApplicationService;

        this.productDealApplicationService =
                productDealApplicationService;
    }

    @GetMapping("/new-arrivals")
    public String showNewArrivalsPage(Model model) {
        List<ProductResponse> newArrivals =
                productApplicationService
                        .listNewArrivals();

        Map<Long, ProductDealResponse>
                activeDealsByProductId =
                productDealApplicationService
                        .listActiveDeals()
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        ProductDealResponse::productId,
                                        Function.identity(),
                                        (firstDeal, secondDeal) ->
                                                firstDeal
                                )
                        );

        List<ProductCardViewModel> productCards =
                newArrivals.stream()
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
                "productCards",
                productCards
        );

        return "public/new-arrivals";
    }
}
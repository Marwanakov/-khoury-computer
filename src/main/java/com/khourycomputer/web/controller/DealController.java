package com.khourycomputer.web.controller;

import com.khourycomputer.application.dto.deal.ProductDealResponse;
import com.khourycomputer.application.service.ProductDealApplicationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DealController {

    private final ProductDealApplicationService
            productDealApplicationService;

    public DealController(
            ProductDealApplicationService productDealApplicationService
    ) {
        this.productDealApplicationService =
                productDealApplicationService;
    }

    @GetMapping("/deals")
    public String showDealsPage(Model model) {
        List<ProductDealResponse> deals =
                productDealApplicationService
                        .listActiveDeals();

        model.addAttribute(
                "deals",
                deals
        );

        return "public/deals";
    }
}
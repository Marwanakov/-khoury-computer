package com.khourycomputer.web.controller.admin;

import com.khourycomputer.application.dto.deal.CreateProductDealRequest;
import com.khourycomputer.application.dto.deal.ProductDealResponse;
import com.khourycomputer.application.dto.deal.UpdateProductDealRequest;
import com.khourycomputer.application.service.ProductApplicationService;
import com.khourycomputer.application.service.ProductDealApplicationService;
import com.khourycomputer.domain.enums.DealStatus;
import com.khourycomputer.web.viewmodel.admin.AdminProductDealForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class AdminProductDealController {

    private final ProductDealApplicationService productDealApplicationService;
    private final ProductApplicationService productApplicationService;

    public AdminProductDealController(
            ProductDealApplicationService productDealApplicationService,
            ProductApplicationService productApplicationService
    ) {
        this.productDealApplicationService =
                productDealApplicationService;

        this.productApplicationService =
                productApplicationService;
    }

    @GetMapping("/admin/deals")
    public String showDealsPage(Model model) {
        List<ProductDealResponse> deals =
                productDealApplicationService.listDeals();

        List<ProductDealResponse> activeDeals = deals.stream()
                .filter(deal ->
                        deal.status() == DealStatus.ACTIVE
                )
                .toList();

        List<ProductDealResponse> scheduledDeals = deals.stream()
                .filter(deal ->
                        deal.status() == DealStatus.SCHEDULED
                )
                .toList();

        List<ProductDealResponse> expiredDeals = deals.stream()
                .filter(deal ->
                        deal.status() == DealStatus.EXPIRED
                )
                .toList();

        long featuredDealCount = activeDeals.stream()
                .filter(ProductDealResponse::featured)
                .count();

        model.addAttribute(
                "activeDeals",
                activeDeals
        );

        model.addAttribute(
                "scheduledDeals",
                scheduledDeals
        );

        model.addAttribute(
                "expiredDeals",
                expiredDeals
        );

        model.addAttribute(
                "totalDealCount",
                deals.size()
        );

        model.addAttribute(
                "activeDealCount",
                activeDeals.size()
        );

        model.addAttribute(
                "scheduledDealCount",
                scheduledDeals.size()
        );

        model.addAttribute(
                "expiredDealCount",
                expiredDeals.size()
        );

        model.addAttribute(
                "featuredDealCount",
                featuredDealCount
        );

        return "admin/deals";
    }

    @GetMapping("/admin/deals/new")
    public String showCreateDealPage(Model model) {
        if (!model.containsAttribute("dealForm")) {
            AdminProductDealForm form =
                    new AdminProductDealForm();

            form.setStartsAt(
                    LocalDateTime.now()
                            .withSecond(0)
                            .withNano(0)
            );

            form.setEndsAt(
                    LocalDateTime.now()
                            .plusDays(7)
                            .withSecond(0)
                            .withNano(0)
            );

            model.addAttribute(
                    "dealForm",
                    form
            );
        }

        prepareCreateForm(model);

        return "admin/deal-form";
    }

    @PostMapping("/admin/deals")
    public String createDeal(
            @Valid
            @ModelAttribute("dealForm")
            AdminProductDealForm dealForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            prepareCreateForm(model);
            return "admin/deal-form";
        }

        try {
            ProductDealResponse createdDeal =
                    productDealApplicationService.createDeal(
                            new CreateProductDealRequest(
                                    dealForm.getProductId(),
                                    dealForm.getDealPrice(),
                                    dealForm.getStartsAt(),
                                    dealForm.getEndsAt(),
                                    dealForm.isFeatured()
                            )
                    );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Deal for \""
                            + createdDeal.productName()
                            + "\" was created successfully."
            );

            return "redirect:/admin/deals";

        } catch (IllegalArgumentException
                 | IllegalStateException exception) {

            bindingResult.reject(
                    "deal.creation.failed",
                    exception.getMessage()
            );

            prepareCreateForm(model);

            return "admin/deal-form";
        }
    }

    @GetMapping("/admin/deals/{dealId}/edit")
    public String showEditDealPage(
            @PathVariable Long dealId,
            Model model
    ) {
        ProductDealResponse deal =
                productDealApplicationService.getDealById(
                        dealId
                );

        if (!model.containsAttribute("dealForm")) {
            model.addAttribute(
                    "dealForm",
                    toAdminProductDealForm(deal)
            );
        }

        prepareEditForm(
                model,
                deal
        );

        return "admin/deal-form";
    }

    @PostMapping("/admin/deals/{dealId}")
    public String updateDeal(
            @PathVariable Long dealId,
            @Valid
            @ModelAttribute("dealForm")
            AdminProductDealForm dealForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        ProductDealResponse existingDeal =
                productDealApplicationService.getDealById(
                        dealId
                );

        if (bindingResult.hasErrors()) {
            prepareEditForm(
                    model,
                    existingDeal
            );

            return "admin/deal-form";
        }

        try {
            ProductDealResponse updatedDeal =
                    productDealApplicationService.updateDeal(
                            dealId,
                            new UpdateProductDealRequest(
                                    dealForm.getDealPrice(),
                                    dealForm.getStartsAt(),
                                    dealForm.getEndsAt(),
                                    dealForm.isFeatured()
                            )
                    );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Deal for \""
                            + updatedDeal.productName()
                            + "\" was updated successfully."
            );

            return "redirect:/admin/deals";

        } catch (IllegalArgumentException
                 | IllegalStateException exception) {

            bindingResult.reject(
                    "deal.update.failed",
                    exception.getMessage()
            );

            prepareEditForm(
                    model,
                    existingDeal
            );

            return "admin/deal-form";
        }
    }

    @PostMapping("/admin/deals/{dealId}/end")
    public String endDeal(
            @PathVariable Long dealId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            ProductDealResponse endedDeal =
                    productDealApplicationService.endDeal(
                            dealId
                    );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Deal for \""
                            + endedDeal.productName()
                            + "\" was ended successfully."
            );

        } catch (IllegalArgumentException
                 | IllegalStateException exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/admin/deals";
    }

    @PostMapping("/admin/deals/{dealId}/delete")
    public String deleteDeal(
            @PathVariable Long dealId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            ProductDealResponse deal =
                    productDealApplicationService.getDealById(
                            dealId
                    );

            productDealApplicationService.deleteDeal(
                    dealId
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Deal for \""
                            + deal.productName()
                            + "\" was deleted successfully."
            );

        } catch (IllegalArgumentException
                 | IllegalStateException exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/admin/deals";
    }

    private void prepareCreateForm(Model model) {
        addProductOptions(model);

        model.addAttribute(
                "formTitle",
                "Create Deal"
        );

        model.addAttribute(
                "formDescription",
                "Select a product, set a genuine discounted price, and choose when the offer should run."
        );

        model.addAttribute(
                "submitLabel",
                "Create Deal"
        );

        model.addAttribute(
                "formAction",
                "/admin/deals"
        );

        model.addAttribute(
                "editing",
                false
        );
    }

    private void prepareEditForm(
            Model model,
            ProductDealResponse deal
    ) {
        addProductOptions(model);

        model.addAttribute(
                "formTitle",
                "Edit Deal"
        );

        model.addAttribute(
                "formDescription",
                "Update the discounted price, schedule, or homepage visibility."
        );

        model.addAttribute(
                "submitLabel",
                "Save Changes"
        );

        model.addAttribute(
                "formAction",
                "/admin/deals/" + deal.id()
        );

        model.addAttribute(
                "editing",
                true
        );

        model.addAttribute(
                "deal",
                deal
        );
    }

    private void addProductOptions(Model model) {
        model.addAttribute(
                "products",
                productApplicationService.listProducts()
        );
    }

    private AdminProductDealForm toAdminProductDealForm(
            ProductDealResponse deal
    ) {
        AdminProductDealForm form =
                new AdminProductDealForm();

        form.setProductId(
                deal.productId()
        );

        form.setDealPrice(
                deal.dealPrice()
        );

        form.setStartsAt(
                deal.startsAt()
        );

        form.setEndsAt(
                deal.endsAt()
        );

        form.setFeatured(
                deal.featured()
        );

        return form;
    }
}
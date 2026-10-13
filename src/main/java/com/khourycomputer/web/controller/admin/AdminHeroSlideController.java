package com.khourycomputer.web.controller.admin;

import com.khourycomputer.application.dto.hero.HeroSlideResponse;
import com.khourycomputer.application.dto.hero.SaveHeroSlideRequest;
import com.khourycomputer.application.dto.product.ProductResponse;
import com.khourycomputer.application.exception.InvalidImageException;
import com.khourycomputer.application.port.storage.ImageUpload;
import com.khourycomputer.application.service.HeroSlideApplicationService;
import com.khourycomputer.application.service.ProductApplicationService;
import com.khourycomputer.domain.enums.HeroLinkType;
import com.khourycomputer.web.viewmodel.admin.AdminHeroSlideForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;
import com.khourycomputer.application.dto.category.CategoryResponse;
import com.khourycomputer.application.service.CategoryApplicationService;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

@Controller
public class AdminHeroSlideController {

        private final HeroSlideApplicationService heroSlideApplicationService;

        private final ProductApplicationService productApplicationService;

        private final CategoryApplicationService categoryApplicationService;

        public AdminHeroSlideController(
                        HeroSlideApplicationService heroSlideApplicationService,
                        ProductApplicationService productApplicationService,
                        CategoryApplicationService categoryApplicationService) {
                this.heroSlideApplicationService = heroSlideApplicationService;

                this.productApplicationService = productApplicationService;

                this.categoryApplicationService = categoryApplicationService;
        }

        @GetMapping("/admin/hero-slides")
        public String showHeroSlidesPage(Model model) {
                List<HeroSlideResponse> slides = heroSlideApplicationService
                                .listAllSlides();

                long activeSlideCount = slides.stream()
                                .filter(HeroSlideResponse::active)
                                .count();

                model.addAttribute("slides", slides);

                model.addAttribute(
                                "totalSlideCount",
                                slides.size());

                model.addAttribute(
                                "activeSlideCount",
                                activeSlideCount);

                model.addAttribute(
                                "inactiveSlideCount",
                                slides.size() - activeSlideCount);

                return "admin/hero-slides";
        }

        @GetMapping("/admin/hero-slides/new")
        public String showCreateSlidePage(Model model) {
                if (!model.containsAttribute("heroSlideForm")) {
                        model.addAttribute(
                                        "heroSlideForm",
                                        new AdminHeroSlideForm());
                }

                prepareFormModel(
                                model,
                                false,
                                null);

                return "admin/hero-slide-form";
        }

        @PostMapping("/admin/hero-slides")
        public String createSlide(
                        @Valid @ModelAttribute("heroSlideForm") AdminHeroSlideForm form,
                        BindingResult bindingResult,
                        Model model,
                        RedirectAttributes redirectAttributes) {
                validateRequiredCreateImages(
                                form,
                                bindingResult);

                if (bindingResult.hasErrors()) {
                        prepareFormModel(
                                        model,
                                        false,
                                        null);

                        return "admin/hero-slide-form";
                }

                try {
                        HeroSlideResponse slide = heroSlideApplicationService.createSlide(
                                        toRequest(form),
                                        toImageUpload(
                                                        form.getDesktopImage()),
                                        toImageUpload(
                                                        form.getMobileImage()));

                        redirectAttributes.addFlashAttribute(
                                        "successMessage",
                                        "Hero slide #"
                                                        + slide.id()
                                                        + " was created successfully.");

                        return "redirect:/admin/hero-slides";

                } catch (InvalidImageException exception) {
                        bindingResult.reject(
                                        "heroSlide.image.invalid",
                                        exception.getMessage());

                } catch (IllegalArgumentException
                                | IllegalStateException exception) {
                        bindingResult.reject(
                                        "heroSlide.create.failed",
                                        exception.getMessage());
                }

                prepareFormModel(
                                model,
                                false,
                                null);

                return "admin/hero-slide-form";
        }

        @GetMapping("/admin/hero-slides/{slideId}/edit")
        public String showEditSlidePage(
                        @PathVariable Long slideId,
                        Model model) {
                HeroSlideResponse slide = heroSlideApplicationService
                                .getSlideById(slideId);

                if (!model.containsAttribute("heroSlideForm")) {
                        model.addAttribute(
                                        "heroSlideForm",
                                        toForm(slide));
                }

                prepareFormModel(
                                model,
                                true,
                                slide);

                return "admin/hero-slide-form";
        }

        @PostMapping("/admin/hero-slides/{slideId}")
        public String updateSlide(
                        @PathVariable Long slideId,
                        @Valid @ModelAttribute("heroSlideForm") AdminHeroSlideForm form,
                        BindingResult bindingResult,
                        Model model,
                        RedirectAttributes redirectAttributes) {
                HeroSlideResponse existingSlide = heroSlideApplicationService
                                .getSlideById(slideId);

                if (bindingResult.hasErrors()) {
                        prepareFormModel(
                                        model,
                                        true,
                                        existingSlide);

                        return "admin/hero-slide-form";
                }

                try {
                        heroSlideApplicationService.updateSlide(
                                        slideId,
                                        toRequest(form),
                                        toImageUpload(
                                                        form.getDesktopImage()),
                                        toImageUpload(
                                                        form.getMobileImage()));

                        redirectAttributes.addFlashAttribute(
                                        "successMessage",
                                        "Hero slide was updated successfully.");

                        return "redirect:/admin/hero-slides";

                } catch (InvalidImageException exception) {
                        bindingResult.reject(
                                        "heroSlide.image.invalid",
                                        exception.getMessage());

                } catch (IllegalArgumentException
                                | IllegalStateException exception) {
                        bindingResult.reject(
                                        "heroSlide.update.failed",
                                        exception.getMessage());
                }

                prepareFormModel(
                                model,
                                true,
                                existingSlide);

                return "admin/hero-slide-form";
        }

        @PostMapping("/admin/hero-slides/{slideId}/delete")
        public String deleteSlide(
                        @PathVariable Long slideId,
                        RedirectAttributes redirectAttributes) {
                try {
                        heroSlideApplicationService.deleteSlide(
                                        slideId);

                        redirectAttributes.addFlashAttribute(
                                        "successMessage",
                                        "Hero slide was deleted successfully.");

                } catch (IllegalArgumentException exception) {
                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        exception.getMessage());
                }

                return "redirect:/admin/hero-slides";
        }

        private void prepareFormModel(
                        Model model,
                        boolean editing,
                        HeroSlideResponse slide) {
                List<ProductResponse> products = productApplicationService
                                .listProducts()
                                .stream()
                                .sorted(
                                                Comparator.comparing(
                                                                ProductResponse::name,
                                                                String.CASE_INSENSITIVE_ORDER))
                                .toList();

                List<CategoryResponse> categories = categoryApplicationService
                                .listCategories()
                                .stream()
                                .sorted(
                                                Comparator.comparing(
                                                                CategoryResponse::name,
                                                                String.CASE_INSENSITIVE_ORDER))
                                .toList();

                model.addAttribute(
                                "products",
                                products);

                model.addAttribute(
                                "categories",
                                categories);

                model.addAttribute(
                                "linkTypes",
                                HeroLinkType.values());

                model.addAttribute(
                                "editing",
                                editing);

                model.addAttribute(
                                "slide",
                                slide);
        }

        private void validateRequiredCreateImages(
                        AdminHeroSlideForm form,
                        BindingResult bindingResult) {
                if (isEmpty(form.getDesktopImage())) {
                        bindingResult.rejectValue(
                                        "desktopImage",
                                        "heroSlide.desktopImage.required",
                                        "Desktop hero image is required.");
                }

                if (isEmpty(form.getMobileImage())) {
                        bindingResult.rejectValue(
                                        "mobileImage",
                                        "heroSlide.mobileImage.required",
                                        "Mobile hero image is required.");
                }
        }

        private boolean isEmpty(MultipartFile file) {
                return file == null || file.isEmpty();
        }

        private SaveHeroSlideRequest toRequest(
                        AdminHeroSlideForm form) {
                return new SaveHeroSlideRequest(
                                form.getAltText() == null
                                                ? ""
                                                : form.getAltText().trim(),
                                form.getLinkType(),
                                form.getLinkedProductId(),
                                form.getLinkedCategoryId(),
                                form.getDisplayOrder(),
                                form.isActive());
        }

        private AdminHeroSlideForm toForm(
                        HeroSlideResponse slide) {
                AdminHeroSlideForm form = new AdminHeroSlideForm();

                form.setAltText(slide.altText());
                form.setLinkType(slide.linkType());

                form.setLinkedProductId(
                                slide.linkedProductId());

                form.setLinkedCategoryId(
                                slide.linkedCategoryId());

                form.setDisplayOrder(
                                slide.displayOrder());

                form.setActive(slide.active());

                return form;
        }

        private ImageUpload toImageUpload(
                        MultipartFile multipartFile) {
                if (multipartFile == null
                                || multipartFile.isEmpty()) {
                        return ImageUpload.empty();
                }

                try {
                        return new ImageUpload(
                                        multipartFile.getBytes());

                } catch (IOException exception) {
                        throw new InvalidImageException(
                                        "The selected image could not be read.",
                                        exception);
                }
        }
}
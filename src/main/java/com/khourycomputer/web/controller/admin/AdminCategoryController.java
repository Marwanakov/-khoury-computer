package com.khourycomputer.web.controller.admin;

import com.khourycomputer.application.dto.category.CategoryResponse;
import com.khourycomputer.application.dto.category.CreateCategoryRequest;
import com.khourycomputer.application.dto.category.UpdateCategoryRequest;
import com.khourycomputer.application.dto.product.ProductResponse;
import com.khourycomputer.application.service.CategoryApplicationService;
import com.khourycomputer.application.service.ProductApplicationService;
import com.khourycomputer.web.viewmodel.admin.AdminCategoryForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.khourycomputer.application.exception.InvalidImageException;
import com.khourycomputer.application.port.storage.ImageUpload;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class AdminCategoryController {

        private final CategoryApplicationService categoryApplicationService;
        private final ProductApplicationService productApplicationService;

        public AdminCategoryController(
                        CategoryApplicationService categoryApplicationService,
                        ProductApplicationService productApplicationService) {
                this.categoryApplicationService = categoryApplicationService;
                this.productApplicationService = productApplicationService;
        }

        @GetMapping("/admin/categories")
        public String showCategoriesPage(
                        @RequestParam(required = false) String keyword,
                        Model model) {
                List<CategoryResponse> allCategories = categoryApplicationService.listCategories()
                                .stream()
                                .sorted(
                                                Comparator.comparing(
                                                                CategoryResponse::name,
                                                                String.CASE_INSENSITIVE_ORDER))
                                .toList();

                List<ProductResponse> products = productApplicationService.listProducts();

                Map<Long, Long> productCountByCategoryId = products.stream()
                                .collect(Collectors.groupingBy(
                                                ProductResponse::categoryId,
                                                Collectors.counting()));

                Set<Long> emptyCategoryIds = allCategories.stream()
                                .filter(category -> productCountByCategoryId.getOrDefault(
                                                category.id(),
                                                0L) == 0)
                                .map(CategoryResponse::id)
                                .collect(Collectors.toSet());

                List<CategoryResponse> filteredCategories = allCategories.stream()
                                .filter(category -> matchesKeyword(category, keyword))
                                .toList();

                long categoriesWithProducts = allCategories.stream()
                                .filter(category -> productCountByCategoryId.getOrDefault(
                                                category.id(),
                                                0L) > 0)
                                .count();

                model.addAttribute(
                                "categories",
                                filteredCategories);
                model.addAttribute(
                                "productCountByCategoryId",
                                productCountByCategoryId);

                model.addAttribute(
                                "emptyCategoryIds",
                                emptyCategoryIds);

                model.addAttribute(
                                "totalCategoryCount",
                                allCategories.size());
                model.addAttribute(
                                "categoriesWithProductsCount",
                                categoriesWithProducts);
                model.addAttribute(
                                "emptyCategoryCount",
                                allCategories.size() - categoriesWithProducts);

                model.addAttribute("keyword", keyword);

                return "admin/categories";
        }

        @GetMapping("/admin/categories/new")
        public String showCreateCategoryPage(Model model) {
                if (!model.containsAttribute("categoryForm")) {
                        model.addAttribute(
                                        "categoryForm",
                                        new AdminCategoryForm());
                }

                prepareCreateForm(model);

                return "admin/category-form";
        }

        @PostMapping("/admin/categories")
        public String createCategory(
                        @Valid @ModelAttribute("categoryForm") AdminCategoryForm categoryForm,
                        BindingResult bindingResult,
                        Model model,
                        RedirectAttributes redirectAttributes) {
                if (bindingResult.hasErrors()) {
                        prepareCreateForm(model);
                        return "admin/category-form";
                }

                try {
                        ImageUpload imageUpload = toImageUpload(
                                        categoryForm.getImage());

                        CategoryResponse category = categoryApplicationService.createCategory(
                                        new CreateCategoryRequest(
                                                        categoryForm.getName().trim(),
                                                        normalizeDescription(
                                                                        categoryForm
                                                                                        .getDescription())),
                                        imageUpload);

                        redirectAttributes.addFlashAttribute(
                                        "successMessage",
                                        "Category \"" + category.name()
                                                        + "\" was created successfully.");

                        return "redirect:/admin/categories";

                } catch (InvalidImageException exception) {
                        bindingResult.rejectValue(
                                        "image",
                                        "category.image.invalid",
                                        exception.getMessage());

                        prepareCreateForm(model);
                        return "admin/category-form";

                } catch (IllegalArgumentException exception) {
                        handleCategoryError(
                                        exception,
                                        bindingResult);

                        prepareCreateForm(model);
                        return "admin/category-form";
                }
        }

        @GetMapping("/admin/categories/{categoryId}/edit")
        public String showEditCategoryPage(
                        @PathVariable Long categoryId,
                        Model model) {
                CategoryResponse category = categoryApplicationService
                                .getCategoryById(categoryId);

                if (!model.containsAttribute("categoryForm")) {
                        AdminCategoryForm form = new AdminCategoryForm();

                        form.setName(category.name());
                        form.setDescription(category.description());

                        model.addAttribute(
                                        "categoryForm",
                                        form);
                }

                prepareEditForm(model, category);

                return "admin/category-form";
        }

        @PostMapping("/admin/categories/{categoryId}")
        public String updateCategory(
                        @PathVariable Long categoryId,
                        @Valid @ModelAttribute("categoryForm") AdminCategoryForm categoryForm,
                        BindingResult bindingResult,
                        Model model,
                        RedirectAttributes redirectAttributes) {
                CategoryResponse existingCategory = categoryApplicationService
                                .getCategoryById(categoryId);

                if (bindingResult.hasErrors()) {
                        prepareEditForm(
                                        model,
                                        existingCategory);

                        return "admin/category-form";
                }

                try {
                        ImageUpload imageUpload = toImageUpload(
                                        categoryForm.getImage());

                        CategoryResponse category = categoryApplicationService.updateCategory(
                                        categoryId,
                                        new UpdateCategoryRequest(
                                                        categoryForm
                                                                        .getName()
                                                                        .trim(),
                                                        normalizeDescription(
                                                                        categoryForm
                                                                                        .getDescription())),
                                        imageUpload,
                                        categoryForm.isRemoveImage());

                        redirectAttributes.addFlashAttribute(
                                        "successMessage",
                                        "Category \"" + category.name()
                                                        + "\" was updated successfully.");

                        return "redirect:/admin/categories";

                } catch (InvalidImageException exception) {
                        bindingResult.rejectValue(
                                        "image",
                                        "category.image.invalid",
                                        exception.getMessage());

                        prepareEditForm(
                                        model,
                                        existingCategory);

                        return "admin/category-form";

                } catch (IllegalArgumentException exception) {
                        handleCategoryError(
                                        exception,
                                        bindingResult);

                        prepareEditForm(
                                        model,
                                        existingCategory);

                        return "admin/category-form";
                }
        }

        @GetMapping("/admin/categories/{categoryId}")
        public String redirectToEditCategory(
                        @PathVariable Long categoryId) {
                return "redirect:/admin/categories/"
                                + categoryId
                                + "/edit";
        }

        @PostMapping("/admin/categories/{categoryId}/delete")
        public String deleteCategory(
                        @PathVariable Long categoryId,
                        RedirectAttributes redirectAttributes) {
                try {
                        CategoryResponse category = categoryApplicationService
                                        .getCategoryById(categoryId);

                        categoryApplicationService.deleteCategory(categoryId);

                        redirectAttributes.addFlashAttribute(
                                        "successMessage",
                                        "Category \"" + category.name()
                                                        + "\" was deleted successfully.");

                } catch (IllegalStateException exception) {
                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        exception.getMessage());
                }

                return "redirect:/admin/categories";
        }

        private void prepareCreateForm(Model model) {
                model.addAttribute(
                                "formTitle",
                                "Add Category");

                model.addAttribute(
                                "formDescription",
                                "Create a new product category for the store.");

                model.addAttribute(
                                "submitLabel",
                                "Create Category");

                model.addAttribute(
                                "formAction",
                                "/admin/categories");

                model.addAttribute(
                                "currentImageUrl",
                                "");
        }

        private void prepareEditForm(
                        Model model,
                        CategoryResponse category) {
                model.addAttribute(
                                "formTitle",
                                "Edit Category");

                model.addAttribute(
                                "formDescription",
                                "Update the category information and storefront image.");

                model.addAttribute(
                                "submitLabel",
                                "Save Changes");

                model.addAttribute(
                                "formAction",
                                "/admin/categories/"
                                                + category.id());

                model.addAttribute(
                                "currentImageUrl",
                                category.imageUrl());
        }

        private void handleCategoryError(
                        IllegalArgumentException exception,
                        BindingResult bindingResult) {
                if ("Category name already exists."
                                .equals(exception.getMessage())) {

                        bindingResult.rejectValue(
                                        "name",
                                        "category.name.duplicate",
                                        exception.getMessage());

                        return;
                }

                bindingResult.reject(
                                "category.operation.failed",
                                exception.getMessage());
        }

        private boolean matchesKeyword(
                        CategoryResponse category,
                        String keyword) {
                if (keyword == null || keyword.isBlank()) {
                        return true;
                }

                String searchText = keyword.trim().toLowerCase(Locale.ROOT);

                return containsIgnoreCase(
                                category.name(),
                                searchText)
                                || containsIgnoreCase(
                                                category.description(),
                                                searchText);
        }

        private boolean containsIgnoreCase(
                        String value,
                        String searchText) {
                return value != null
                                && value.toLowerCase(Locale.ROOT)
                                                .contains(searchText);
        }

        private String normalizeDescription(String description) {
                return description == null
                                ? ""
                                : description.trim();
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
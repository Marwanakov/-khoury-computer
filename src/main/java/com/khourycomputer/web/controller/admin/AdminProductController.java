package com.khourycomputer.web.controller.admin;

import com.khourycomputer.application.dto.category.CategoryResponse;
import com.khourycomputer.application.dto.product.CreateProductRequest;
import com.khourycomputer.application.dto.product.ProductResponse;
import com.khourycomputer.application.dto.product.UpdateProductRequest;
import com.khourycomputer.application.service.CategoryApplicationService;
import com.khourycomputer.application.service.ProductApplicationService;
import com.khourycomputer.domain.enums.ProductAvailabilityStatus;
import com.khourycomputer.web.viewmodel.admin.AdminProductForm;
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class AdminProductController {

        private final ProductApplicationService productApplicationService;
        private final CategoryApplicationService categoryApplicationService;

        public AdminProductController(
                        ProductApplicationService productApplicationService,
                        CategoryApplicationService categoryApplicationService) {
                this.productApplicationService = productApplicationService;
                this.categoryApplicationService = categoryApplicationService;
        }

        @GetMapping("/admin/products")
        public String showProductsPage(
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) Long categoryId,
                        @RequestParam(required = false) ProductAvailabilityStatus status,
                        @RequestParam(required = false) BigDecimal minPrice,
                        @RequestParam(required = false) BigDecimal maxPrice,
                        Model model) {
                List<ProductResponse> products = productApplicationService.filterProducts(
                                keyword,
                                categoryId,
                                null,
                                status,
                                minPrice,
                                maxPrice);

                List<ProductResponse> allProducts = productApplicationService.listProducts();

                List<CategoryResponse> categories = categoryApplicationService.listCategories();

                Map<Long, CategoryResponse> categoriesById = categories.stream()
                                .collect(Collectors.toMap(
                                                CategoryResponse::id,
                                                Function.identity()));

                long availableCount = allProducts.stream()
                                .filter(product -> product.availabilityStatus() == ProductAvailabilityStatus.AVAILABLE)
                                .count();

                long lowStockCount = allProducts.stream()
                                .filter(product -> product.availabilityStatus() == ProductAvailabilityStatus.LOW_STOCK)
                                .count();

                long soldOutCount = allProducts.stream()
                                .filter(product -> product.availabilityStatus() == ProductAvailabilityStatus.SOLD_OUT)
                                .count();

                model.addAttribute("products", products);
                model.addAttribute("categories", categories);
                model.addAttribute("categoriesById", categoriesById);
                model.addAttribute(
                                "availabilityStatuses",
                                ProductAvailabilityStatus.values());

                model.addAttribute("totalProductCount", allProducts.size());
                model.addAttribute("availableCount", availableCount);
                model.addAttribute("lowStockCount", lowStockCount);
                model.addAttribute("soldOutCount", soldOutCount);

                model.addAttribute("keyword", keyword);
                model.addAttribute("selectedCategoryId", categoryId);
                model.addAttribute("selectedStatus", status);
                model.addAttribute("minPrice", minPrice);
                model.addAttribute("maxPrice", maxPrice);

                return "admin/products";
        }

        @GetMapping("/admin/products/new")
        public String showCreateProductPage(Model model) {
                if (!model.containsAttribute("productForm")) {
                        model.addAttribute(
                                        "productForm",
                                        new AdminProductForm());
                }

                addFormOptions(model);

                model.addAttribute("formTitle", "Add Product");
                model.addAttribute(
                                "formDescription",
                                "Create a new product for the Khoury Computer catalog.");
                model.addAttribute("submitLabel", "Create Product");
                model.addAttribute("formAction", "/admin/products");

                return "admin/product-form";
        }

        @PostMapping("/admin/products")
        public String createProduct(
                        @Valid @ModelAttribute("productForm") AdminProductForm productForm,
                        BindingResult bindingResult,
                        Model model,
                        RedirectAttributes redirectAttributes) {
                if (bindingResult.hasErrors()) {
                        prepareCreateForm(model);
                        return "admin/product-form";
                }

                try {
                        ProductResponse createdProduct = productApplicationService.createProduct(
                                        toCreateProductRequest(productForm));

                        redirectAttributes.addFlashAttribute(
                                        "successMessage",
                                        "Product \"" + createdProduct.name()
                                                        + "\" was created successfully.");

                        return "redirect:/admin/products";

                } catch (IllegalArgumentException exception) {
                        bindingResult.reject(
                                        "product.creation.failed",
                                        exception.getMessage());

                        prepareCreateForm(model);

                        return "admin/product-form";
                }
        }

        @GetMapping("/admin/products/{productId}")
        public String redirectToEditProductPage(
                        @PathVariable Long productId) {
                return "redirect:/admin/products/"
                                + productId
                                + "/edit";
        }

        @GetMapping("/admin/products/{productId}/edit")
        public String showEditProductPage(
                        @PathVariable Long productId,
                        Model model) {
                ProductResponse product = productApplicationService.getProductById(productId);

                if (!model.containsAttribute("productForm")) {
                        model.addAttribute(
                                        "productForm",
                                        toAdminProductForm(product));
                }

                prepareEditForm(model, productId);

                return "admin/product-form";
        }

        @PostMapping("/admin/products/{productId}")
        public String updateProduct(
                        @PathVariable Long productId,
                        @Valid @ModelAttribute("productForm") AdminProductForm productForm,
                        BindingResult bindingResult,
                        Model model,
                        RedirectAttributes redirectAttributes) {
                if (bindingResult.hasErrors()) {
                        prepareEditForm(model, productId);
                        return "admin/product-form";
                }

                try {
                        ProductResponse updatedProduct = productApplicationService.updateProduct(
                                        productId,
                                        toUpdateProductRequest(productForm));

                        redirectAttributes.addFlashAttribute(
                                        "successMessage",
                                        "Product \"" + updatedProduct.name()
                                                        + "\" was updated successfully.");

                        return "redirect:/admin/products";

                } catch (IllegalArgumentException exception) {
                        bindingResult.reject(
                                        "product.update.failed",
                                        exception.getMessage());

                        prepareEditForm(model, productId);

                        return "admin/product-form";
                }
        }

        private void prepareCreateForm(Model model) {
                addFormOptions(model);

                model.addAttribute("formTitle", "Add Product");
                model.addAttribute(
                                "formDescription",
                                "Create a new product for the Khoury Computer catalog.");
                model.addAttribute("submitLabel", "Create Product");
                model.addAttribute("formAction", "/admin/products");
        }

        private void addFormOptions(Model model) {
                model.addAttribute(
                                "categories",
                                categoryApplicationService.listCategories());
        }

        private CreateProductRequest toCreateProductRequest(
                        AdminProductForm form) {
                return new CreateProductRequest(
                                form.getName().trim(),
                                normalizeOptionalText(form.getDescription()),
                                form.getPrice(),
                                form.getBrand().trim(),
                                form.getStockQuantity(),
                                normalizeOptionalText(form.getImageUrl()),
                                form.getCategoryId(),
                                parseTags(form.getTags()));
        }

        private Set<String> parseTags(String tags) {
                if (tags == null || tags.isBlank()) {
                        return Set.of();
                }

                return Arrays.stream(tags.split(","))
                                .map(String::trim)
                                .filter(tag -> !tag.isBlank())
                                .map(String::toLowerCase)
                                .collect(Collectors.toSet());
        }

        private String normalizeOptionalText(String value) {
                return value == null ? "" : value.trim();
        }

        private void prepareEditForm(
                        Model model,
                        Long productId) {
                addFormOptions(model);

                model.addAttribute("formTitle", "Edit Product");
                model.addAttribute(
                                "formDescription",
                                "Update the product information, price, stock, and catalog details.");
                model.addAttribute("submitLabel", "Save Changes");
                model.addAttribute(
                                "formAction",
                                "/admin/products/" + productId);
        }

        private AdminProductForm toAdminProductForm(
                        ProductResponse product) {
                AdminProductForm form = new AdminProductForm();

                form.setName(product.name());
                form.setDescription(product.description());
                form.setPrice(product.price());
                form.setBrand(product.brand());
                form.setStockQuantity(product.stockQuantity());
                form.setImageUrl(product.imageUrl());
                form.setCategoryId(product.categoryId());
                form.setTags(String.join(", ", product.tags()));

                return form;
        }

        private UpdateProductRequest toUpdateProductRequest(
                        AdminProductForm form) {
                return new UpdateProductRequest(
                                form.getName().trim(),
                                normalizeOptionalText(form.getDescription()),
                                form.getPrice(),
                                form.getBrand().trim(),
                                form.getStockQuantity(),
                                normalizeOptionalText(form.getImageUrl()),
                                form.getCategoryId(),
                                parseTags(form.getTags()));
        }
}
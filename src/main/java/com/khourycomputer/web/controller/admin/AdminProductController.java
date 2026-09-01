package com.khourycomputer.web.controller.admin;

import com.khourycomputer.application.dto.category.CategoryResponse;
import com.khourycomputer.application.dto.product.CreateProductRequest;
import com.khourycomputer.application.dto.product.ProductResponse;
import com.khourycomputer.application.dto.product.UpdateProductRequest;
import com.khourycomputer.application.exception.InvalidImageException;
import com.khourycomputer.application.port.storage.ImageUpload;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.Comparator;
import com.khourycomputer.application.service.ProductDealApplicationService;

@Controller
public class AdminProductController {

        private final ProductApplicationService productApplicationService;
        private final CategoryApplicationService categoryApplicationService;
        private final ProductDealApplicationService productDealApplicationService;

        public AdminProductController(
                        ProductApplicationService productApplicationService,
                        CategoryApplicationService categoryApplicationService,
                        ProductDealApplicationService productDealApplicationService) {
                this.productApplicationService = productApplicationService;

                this.categoryApplicationService = categoryApplicationService;

                this.productDealApplicationService = productDealApplicationService;
        }

        @GetMapping("/admin/products")
        public String showProductsPage(
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) Long categoryId,
                        @RequestParam(required = false) ProductAvailabilityStatus status,
                        @RequestParam(required = false) BigDecimal minPrice,
                        @RequestParam(required = false) BigDecimal maxPrice,
                        @RequestParam(required = false) String collection,
                        Model model) {
                List<ProductResponse> products = productApplicationService.filterProducts(
                                keyword,
                                categoryId,
                                null,
                                status,
                                minPrice,
                                maxPrice);

                List<ProductResponse> allProducts = productApplicationService.listProducts();

                Set<Long> activeDealProductIds = productDealApplicationService
                                .listActiveDeals()
                                .stream()
                                .map(deal -> deal.productId())
                                .collect(Collectors.toSet());

                String selectedCollection = normalizeAdminCollection(collection);

                if ("DEALS".equals(selectedCollection)) {
                        products = products.stream()
                                        .filter(product -> activeDealProductIds.contains(
                                                        product.id()))
                                        .toList();
                }

                if ("NEW_ARRIVALS".equals(selectedCollection)) {
                        products = products.stream()
                                        .filter(ProductResponse::newArrival)
                                        .sorted(
                                                        Comparator.comparing(
                                                                        ProductResponse::newArrivalMarkedAt).reversed())
                                        .toList();
                }

                List<CategoryResponse> categories = categoryApplicationService.listCategories();

                Map<Long, CategoryResponse> categoriesById = categories.stream()
                                .collect(
                                                Collectors.toMap(
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

                long newArrivalCount = allProducts.stream()
                                .filter(ProductResponse::newArrival)
                                .count();

                model.addAttribute("products", products);
                model.addAttribute("categories", categories);
                model.addAttribute("categoriesById", categoriesById);
                model.addAttribute(
                                "availabilityStatuses",
                                ProductAvailabilityStatus.values());

                model.addAttribute(
                                "totalProductCount",
                                allProducts.size());
                model.addAttribute("availableCount", availableCount);
                model.addAttribute("lowStockCount", lowStockCount);
                model.addAttribute("soldOutCount", soldOutCount);
                model.addAttribute("newArrivalCount", newArrivalCount);

                model.addAttribute("keyword", keyword);
                model.addAttribute(
                                "selectedCategoryId",
                                categoryId);
                model.addAttribute("selectedStatus", status);
                model.addAttribute("minPrice", minPrice);
                model.addAttribute("maxPrice", maxPrice);
                model.addAttribute(
                                "selectedCollection",
                                selectedCollection);
                model.addAttribute(
                                "activeDealProductIds",
                                activeDealProductIds);

                return "admin/products";
        }

        @GetMapping("/admin/products/new")
        public String showCreateProductPage(Model model) {
                if (!model.containsAttribute("productForm")) {
                        model.addAttribute(
                                        "productForm",
                                        new AdminProductForm());
                }

                prepareCreateForm(model);

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
                        ImageUpload imageUpload = toImageUpload(productForm.getImage());

                        ProductResponse createdProduct = productApplicationService.createProduct(
                                        toCreateProductRequest(productForm),
                                        imageUpload);

                        redirectAttributes.addFlashAttribute(
                                        "successMessage",
                                        "Product \""
                                                        + createdProduct.name()
                                                        + "\" was created successfully.");

                        return "redirect:/admin/products";

                } catch (InvalidImageException exception) {
                        bindingResult.rejectValue(
                                        "image",
                                        "product.image.invalid",
                                        exception.getMessage());

                        prepareCreateForm(model);

                        return "admin/product-form";

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
                ProductResponse product = productApplicationService.getProductById(
                                productId);

                if (!model.containsAttribute("productForm")) {
                        model.addAttribute(
                                        "productForm",
                                        toAdminProductForm(product));
                }

                prepareEditForm(model, product);

                return "admin/product-form";
        }

        @PostMapping("/admin/products/{productId}")
        public String updateProduct(
                        @PathVariable Long productId,
                        @Valid @ModelAttribute("productForm") AdminProductForm productForm,
                        BindingResult bindingResult,
                        Model model,
                        RedirectAttributes redirectAttributes) {
                ProductResponse existingProduct = productApplicationService.getProductById(
                                productId);

                if (bindingResult.hasErrors()) {
                        prepareEditForm(
                                        model,
                                        existingProduct);

                        return "admin/product-form";
                }

                try {
                        ImageUpload imageUpload = toImageUpload(productForm.getImage());

                        ProductResponse updatedProduct = productApplicationService.updateProduct(
                                        productId,
                                        toUpdateProductRequest(productForm),
                                        imageUpload,
                                        productForm.isRemoveImage());

                        redirectAttributes.addFlashAttribute(
                                        "successMessage",
                                        "Product \""
                                                        + updatedProduct.name()
                                                        + "\" was updated successfully.");

                        return "redirect:/admin/products";

                } catch (InvalidImageException exception) {
                        bindingResult.rejectValue(
                                        "image",
                                        "product.image.invalid",
                                        exception.getMessage());

                        prepareEditForm(
                                        model,
                                        existingProduct);

                        return "admin/product-form";

                } catch (IllegalArgumentException exception) {
                        bindingResult.reject(
                                        "product.update.failed",
                                        exception.getMessage());

                        prepareEditForm(
                                        model,
                                        existingProduct);

                        return "admin/product-form";
                }
        }

        private void prepareCreateForm(Model model) {
                addFormOptions(model);

                model.addAttribute(
                                "formTitle",
                                "Add Product");

                model.addAttribute(
                                "formDescription",
                                "Create a new product for the Khoury Computer catalog.");

                model.addAttribute(
                                "submitLabel",
                                "Create Product");

                model.addAttribute(
                                "formAction",
                                "/admin/products");

                model.addAttribute(
                                "currentImageUrl",
                                "");
        }

        private void prepareEditForm(
                        Model model,
                        ProductResponse product) {
                addFormOptions(model);

                model.addAttribute(
                                "formTitle",
                                "Edit Product");

                model.addAttribute(
                                "formDescription",
                                "Update the product information, inventory, and storefront image.");

                model.addAttribute(
                                "submitLabel",
                                "Save Changes");

                model.addAttribute(
                                "formAction",
                                "/admin/products/" + product.id());

                model.addAttribute(
                                "currentImageUrl",
                                product.imageUrl());
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
                                normalizeOptionalText(form.getSpecifications()),
                                form.getPrice(),
                                form.getBrand().trim(),
                                form.getStockQuantity(),
                                form.getCategoryId(),
                                parseTags(form.getTags()),
                                form.isNewArrival());
        }

        private UpdateProductRequest toUpdateProductRequest(
                        AdminProductForm form) {
                return new UpdateProductRequest(
                                form.getName().trim(),
                                normalizeOptionalText(form.getDescription()),
                                normalizeOptionalText(form.getSpecifications()),
                                form.getPrice(),
                                form.getBrand().trim(),
                                form.getStockQuantity(),
                                form.getCategoryId(),
                                parseTags(form.getTags()),
                                form.isNewArrival());
        }

        private AdminProductForm toAdminProductForm(
                        ProductResponse product) {
                AdminProductForm form = new AdminProductForm();

                form.setName(product.name());
                form.setDescription(product.description());
                form.setSpecifications(product.specifications());
                form.setPrice(product.price());
                form.setBrand(product.brand());
                form.setStockQuantity(product.stockQuantity());
                form.setCategoryId(product.categoryId());
                form.setTags(String.join(", ", product.tags()));
                form.setNewArrival(product.newArrival());

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

        private Set<String> parseTags(String tags) {
                if (tags == null || tags.isBlank()) {
                        return Set.of();
                }

                return Arrays.stream(tags.split(","))
                                .map(String::trim)
                                .filter(tag -> !tag.isBlank())
                                .map(tag -> tag.toLowerCase(Locale.ROOT))
                                .collect(Collectors.toSet());
        }

        private String normalizeOptionalText(
                        String value) {
                return value == null
                                ? ""
                                : value.trim();
        }

        private String normalizeAdminCollection(
                        String collection) {
                if ("DEALS".equalsIgnoreCase(collection)) {
                        return "DEALS";
                }

                if ("NEW_ARRIVALS".equalsIgnoreCase(collection)) {
                        return "NEW_ARRIVALS";
                }

                return "";
        }
}
package com.khourycomputer.web.controller.admin;

import com.khourycomputer.application.dto.order.OrderResponse;
import com.khourycomputer.application.service.OrderApplicationService;
import com.khourycomputer.application.service.ProductApplicationService;
import com.khourycomputer.domain.enums.OrderStatus;
import com.khourycomputer.web.viewmodel.admin.AdminOrderDiscountForm;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class AdminOrderController {

        private final OrderApplicationService orderApplicationService;
        private final ProductApplicationService productApplicationService;

        public AdminOrderController(
                        OrderApplicationService orderApplicationService,
                        ProductApplicationService productApplicationService) {
                this.orderApplicationService = orderApplicationService;
                this.productApplicationService = productApplicationService;
        }

        @GetMapping("/admin/orders")
        public String showOrdersPage(
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) OrderStatus status,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
                        Model model) {
                List<OrderResponse> allOrders = orderApplicationService.listOrders();

                List<OrderResponse> filteredOrders = allOrders.stream()
                                .filter(order -> matchesKeyword(order, keyword))
                                .filter(order -> status == null || order.status() == status)
                                .filter(order -> dateFrom == null
                                                || !order.createdAt()
                                                                .toLocalDate()
                                                                .isBefore(dateFrom))
                                .filter(order -> dateTo == null
                                                || !order.createdAt()
                                                                .toLocalDate()
                                                                .isAfter(dateTo))
                                .sorted(
                                                Comparator.comparing(
                                                                OrderResponse::createdAt).reversed())
                                .toList();

                model.addAttribute("orders", filteredOrders);
                model.addAttribute("orderStatuses", OrderStatus.values());

                model.addAttribute("totalOrderCount", allOrders.size());
                model.addAttribute(
                                "pendingCount",
                                countByStatus(allOrders, OrderStatus.PENDING));
                model.addAttribute(
                                "confirmedCount",
                                countByStatus(allOrders, OrderStatus.CONFIRMED));
                model.addAttribute(
                                "completedCount",
                                countByStatus(allOrders, OrderStatus.COMPLETED));
                model.addAttribute(
                                "cancelledCount",
                                countByStatus(allOrders, OrderStatus.CANCELLED));

                model.addAttribute("keyword", keyword);
                model.addAttribute("selectedStatus", status);
                model.addAttribute("dateFrom", dateFrom);
                model.addAttribute("dateTo", dateTo);

                return "admin/orders";
        }

        @GetMapping("/admin/orders/{orderId}")
        public String showOrderDetailsPage(
                        @PathVariable Long orderId,
                        Model model) {
                OrderResponse order = orderApplicationService.getOrderById(orderId);

                BigDecimal currentAgreedTotal = order.hasCustomDiscount()
                                ? order.totalPrice()
                                : null;

                model.addAttribute(
                                "discountForm",
                                new AdminOrderDiscountForm(
                                                currentAgreedTotal));

                Map<Long, Integer> currentStockByProductId = order.items()
                                .stream()
                                .map(item -> item.productId())
                                .distinct()
                                .collect(Collectors.toMap(
                                                Function.identity(),
                                                productId -> productApplicationService
                                                                .getProductById(productId)
                                                                .stockQuantity()));

                model.addAttribute("order", order);
                model.addAttribute(
                                "currentStockByProductId",
                                currentStockByProductId);

                return "admin/order-details";
        }

        private long countByStatus(
                        List<OrderResponse> orders,
                        OrderStatus status) {
                return orders.stream()
                                .filter(order -> order.status() == status)
                                .count();
        }

        private boolean matchesKeyword(
                        OrderResponse order,
                        String keyword) {
                if (keyword == null || keyword.isBlank()) {
                        return true;
                }

                String searchText = keyword.trim().toLowerCase(Locale.ROOT);

                return order.id().toString().contains(searchText)
                                || containsIgnoreCase(
                                                order.customerName(),
                                                searchText)
                                || containsIgnoreCase(
                                                order.customerEmail(),
                                                searchText)
                                || containsIgnoreCase(
                                                order.customerPhoneNumber(),
                                                searchText);
        }

        private boolean containsIgnoreCase(
                        String value,
                        String searchText) {
                return value != null
                                && value.toLowerCase(Locale.ROOT)
                                                .contains(searchText);
        }

        @PostMapping("/admin/orders/{orderId}/confirm")
        public String confirmOrder(
                        @PathVariable Long orderId,
                        RedirectAttributes redirectAttributes) {
                try {
                        orderApplicationService.confirmOrder(orderId);

                        redirectAttributes.addFlashAttribute(
                                        "successMessage",
                                        "Order #" + orderId
                                                        + " was confirmed successfully. "
                                                        + "Product stock was updated.");

                } catch (IllegalStateException exception) {
                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        exception.getMessage());
                }

                return "redirect:/admin/orders/" + orderId;
        }

        @PostMapping("/admin/orders/{orderId}/complete")
        public String completeOrder(
                        @PathVariable Long orderId,
                        RedirectAttributes redirectAttributes) {
                try {
                        orderApplicationService.completeOrder(orderId);

                        redirectAttributes.addFlashAttribute(
                                        "successMessage",
                                        "Order #" + orderId
                                                        + " was completed successfully.");

                } catch (IllegalStateException exception) {
                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        exception.getMessage());
                }

                return "redirect:/admin/orders/" + orderId;
        }

        @PostMapping("/admin/orders/{orderId}/cancel")
        public String cancelOrder(
                        @PathVariable Long orderId,
                        RedirectAttributes redirectAttributes) {
                try {
                        orderApplicationService.cancelOrder(orderId);

                        redirectAttributes.addFlashAttribute(
                                        "successMessage",
                                        "Order #" + orderId
                                                        + " was cancelled successfully.");

                } catch (IllegalStateException exception) {
                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        exception.getMessage());
                }

                return "redirect:/admin/orders/" + orderId;
        }

        @PostMapping("/admin/orders/{orderId}/custom-discount")
        public String applyCustomDiscount(
                        @PathVariable Long orderId,
                        @ModelAttribute("discountForm") AdminOrderDiscountForm discountForm,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes) {

                if (bindingResult.hasErrors()
                                || discountForm.getAgreedFinalTotal() == null) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "Enter a valid agreed final total.");

                        return "redirect:/admin/orders/" + orderId;
                }

                try {
                        orderApplicationService.applyCustomDiscount(
                                        orderId,
                                        discountForm.getAgreedFinalTotal());

                        redirectAttributes.addFlashAttribute(
                                        "successMessage",
                                        "The custom price for order #"
                                                        + orderId
                                                        + " was saved successfully.");

                } catch (IllegalArgumentException
                                | IllegalStateException exception) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        exception.getMessage());
                }

                return "redirect:/admin/orders/" + orderId;
        }

        @PostMapping("/admin/orders/{orderId}/custom-discount/remove")
        public String removeCustomDiscount(
                        @PathVariable Long orderId,
                        RedirectAttributes redirectAttributes) {

                try {
                        orderApplicationService.removeCustomDiscount(
                                        orderId);

                        redirectAttributes.addFlashAttribute(
                                        "successMessage",
                                        "The custom discount for order #"
                                                        + orderId
                                                        + " was removed successfully.");

                } catch (IllegalStateException exception) {
                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        exception.getMessage());
                }

                return "redirect:/admin/orders/" + orderId;
        }
}
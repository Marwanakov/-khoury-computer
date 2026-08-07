package com.khourycomputer.web.controller.admin;

import com.khourycomputer.application.dto.order.OrderResponse;
import com.khourycomputer.application.dto.product.ProductResponse;
import com.khourycomputer.application.dto.user.UserResponse;
import com.khourycomputer.application.service.OrderApplicationService;
import com.khourycomputer.application.service.ProductApplicationService;
import com.khourycomputer.application.service.UserApplicationService;
import com.khourycomputer.domain.enums.OrderStatus;
import com.khourycomputer.domain.enums.ProductAvailabilityStatus;
import com.khourycomputer.domain.enums.UserRole;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Comparator;
import java.util.List;

@Controller
public class AdminOverviewController {

    private static final int RECENT_ORDER_LIMIT = 5;
    private static final int INVENTORY_ALERT_LIMIT = 5;

    private final ProductApplicationService productApplicationService;
    private final OrderApplicationService orderApplicationService;
    private final UserApplicationService userApplicationService;

    public AdminOverviewController(
            ProductApplicationService productApplicationService,
            OrderApplicationService orderApplicationService,
            UserApplicationService userApplicationService
    ) {
        this.productApplicationService = productApplicationService;
        this.orderApplicationService = orderApplicationService;
        this.userApplicationService = userApplicationService;
    }

    @GetMapping("/admin/overview")
    public String showOverviewPage(Model model) {
        List<ProductResponse> products =
                productApplicationService.listProducts();

        List<OrderResponse> orders =
                orderApplicationService.listOrders();

        List<UserResponse> customers =
                userApplicationService.listUsers()
                        .stream()
                        .filter(user ->
                                user.role() == UserRole.CUSTOMER
                        )
                        .toList();

        long pendingOrderCount =
                countOrdersByStatus(
                        orders,
                        OrderStatus.PENDING
                );

        long lowStockProductCount =
                countProductsByStatus(
                        products,
                        ProductAvailabilityStatus.LOW_STOCK
                );

        long soldOutProductCount =
                countProductsByStatus(
                        products,
                        ProductAvailabilityStatus.SOLD_OUT
                );

        List<OrderResponse> recentOrders =
                orders.stream()
                        .sorted(
                                Comparator.comparing(
                                        OrderResponse::createdAt
                                ).reversed()
                        )
                        .limit(RECENT_ORDER_LIMIT)
                        .toList();

        List<ProductResponse> inventoryAlerts =
                products.stream()
                        .filter(product ->
                                product.availabilityStatus()
                                        == ProductAvailabilityStatus.LOW_STOCK
                                || product.availabilityStatus()
                                        == ProductAvailabilityStatus.SOLD_OUT
                        )
                        .sorted(
                                Comparator.comparingInt(
                                        ProductResponse::stockQuantity
                                )
                        )
                        .limit(INVENTORY_ALERT_LIMIT)
                        .toList();

        model.addAttribute(
                "totalProductCount",
                products.size()
        );
        model.addAttribute(
                "totalCustomerCount",
                customers.size()
        );
        model.addAttribute(
                "totalOrderCount",
                orders.size()
        );
        model.addAttribute(
                "pendingOrderCount",
                pendingOrderCount
        );
        model.addAttribute(
                "lowStockProductCount",
                lowStockProductCount
        );
        model.addAttribute(
                "soldOutProductCount",
                soldOutProductCount
        );

        model.addAttribute("recentOrders", recentOrders);
        model.addAttribute(
                "inventoryAlerts",
                inventoryAlerts
        );

        return "admin/overview";
    }

    private long countOrdersByStatus(
            List<OrderResponse> orders,
            OrderStatus status
    ) {
        return orders.stream()
                .filter(order -> order.status() == status)
                .count();
    }

    private long countProductsByStatus(
            List<ProductResponse> products,
            ProductAvailabilityStatus status
    ) {
        return products.stream()
                .filter(product ->
                        product.availabilityStatus() == status
                )
                .count();
    }
}
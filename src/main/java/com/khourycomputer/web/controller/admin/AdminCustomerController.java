package com.khourycomputer.web.controller.admin;

import com.khourycomputer.application.dto.order.OrderResponse;
import com.khourycomputer.application.dto.user.UserResponse;
import com.khourycomputer.application.service.OrderApplicationService;
import com.khourycomputer.application.service.UserApplicationService;
import com.khourycomputer.domain.enums.OrderStatus;
import com.khourycomputer.domain.enums.UserRole;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class AdminCustomerController {

    private final UserApplicationService userApplicationService;
    private final OrderApplicationService orderApplicationService;

    public AdminCustomerController(
            UserApplicationService userApplicationService,
            OrderApplicationService orderApplicationService) {
        this.userApplicationService = userApplicationService;
        this.orderApplicationService = orderApplicationService;
    }

    @GetMapping("/admin/customers")
    public String showCustomersPage(
            @RequestParam(required = false) String keyword,
            Model model) {

        List<UserResponse> allCustomers = userApplicationService.listUsers()
                .stream()
                .filter(user -> user.role() == UserRole.CUSTOMER)
                .toList();

        Set<Long> customerIds = allCustomers.stream()
                .map(UserResponse::id)
                .collect(Collectors.toSet());

        List<OrderResponse> customerOrders =
                orderApplicationService.listOrders()
                        .stream()
                        .filter(order -> customerIds.contains(
                                order.userId()))
                        .toList();

        Map<Long, Long> orderCountByCustomerId =
                customerOrders.stream()
                        .collect(Collectors.groupingBy(
                                OrderResponse::userId,
                                Collectors.counting()));

        Map<Long, OrderResponse> latestOrderByCustomerId =
                customerOrders.stream()
                        .sorted(
                                Comparator.comparing(
                                        OrderResponse::createdAt)
                                        .reversed())
                        .collect(Collectors.toMap(
                                OrderResponse::userId,
                                order -> order,
                                (latestOrder, ignoredOrder) ->
                                        latestOrder));

        Set<Long> customersWithOrders =
                customerOrders.stream()
                        .map(OrderResponse::userId)
                        .collect(Collectors.toSet());

        List<UserResponse> filteredCustomers =
                allCustomers.stream()
                        .filter(customer -> matchesKeyword(
                                customer,
                                latestOrderByCustomerId.get(
                                        customer.id()),
                                keyword))
                        .sorted(
                                Comparator.comparing(
                                        UserResponse::fullName,
                                        String.CASE_INSENSITIVE_ORDER))
                        .toList();

        model.addAttribute(
                "customers",
                filteredCustomers);

        model.addAttribute(
                "orderCountByCustomerId",
                orderCountByCustomerId);

        model.addAttribute(
                "latestOrderByCustomerId",
                latestOrderByCustomerId);

        model.addAttribute(
                "totalCustomerCount",
                allCustomers.size());

        model.addAttribute(
                "customersWithOrdersCount",
                customersWithOrders.size());

        model.addAttribute(
                "totalCustomerOrderCount",
                customerOrders.size());

        model.addAttribute("keyword", keyword);

        return "admin/customers";
    }

    @GetMapping("/admin/customers/{customerId}")
    public String showCustomerDetailsPage(
            @PathVariable Long customerId,
            Model model) {
        UserResponse customer =
                userApplicationService.getCustomerById(customerId);

        List<OrderResponse> orders =
                orderApplicationService
                        .listOrdersByUserId(customerId)
                        .stream()
                        .sorted(
                                Comparator.comparing(
                                        OrderResponse::createdAt)
                                        .reversed())
                        .toList();

        BigDecimal totalOrderValue =
                orders.stream()
                        .filter(order ->
                                order.status()
                                        != OrderStatus.CANCELLED)
                        .map(OrderResponse::totalPrice)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add);

        model.addAttribute("customer", customer);
        model.addAttribute("orders", orders);

        model.addAttribute(
                "latestOrder",
                orders.isEmpty() ? null : orders.getFirst());

        model.addAttribute("totalOrderCount", orders.size());

        model.addAttribute(
                "pendingOrderCount",
                countOrdersByStatus(
                        orders,
                        OrderStatus.PENDING));

        model.addAttribute(
                "confirmedOrderCount",
                countOrdersByStatus(
                        orders,
                        OrderStatus.CONFIRMED));

        model.addAttribute(
                "completedOrderCount",
                countOrdersByStatus(
                        orders,
                        OrderStatus.COMPLETED));

        model.addAttribute(
                "cancelledOrderCount",
                countOrdersByStatus(
                        orders,
                        OrderStatus.CANCELLED));

        model.addAttribute(
                "totalOrderValue",
                totalOrderValue);

        return "admin/customer-details";
    }

    private long countOrdersByStatus(
            List<OrderResponse> orders,
            OrderStatus status) {
        return orders.stream()
                .filter(order -> order.status() == status)
                .count();
    }

    private boolean matchesKeyword(
            UserResponse customer,
            OrderResponse latestOrder,
            String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }

        String searchText =
                keyword.trim().toLowerCase(Locale.ROOT);

        String phoneNumber = latestOrder == null
                ? null
                : latestOrder.customerPhoneNumber();

        String city = latestOrder == null
                ? null
                : latestOrder.deliveryAddress().city();

        return customer.id().toString().contains(searchText)
                || containsIgnoreCase(
                        customer.fullName(),
                        searchText)
                || containsIgnoreCase(
                        customer.email(),
                        searchText)
                || containsIgnoreCase(
                        phoneNumber,
                        searchText)
                || containsIgnoreCase(
                        city,
                        searchText);
    }

    private boolean containsIgnoreCase(
            String value,
            String searchText) {
        return value != null
                && value.toLowerCase(Locale.ROOT)
                        .contains(searchText);
    }
}
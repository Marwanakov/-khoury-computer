package com.khourycomputer.web.exception;

import com.khourycomputer.domain.exception.CustomerNotFoundException;
import com.khourycomputer.domain.exception.OrderNotFoundException;
import com.khourycomputer.domain.exception.ProductNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleOrderNotFound(
            OrderNotFoundException exception,
            Model model) {
        model.addAttribute("errorTitle", "Order Not Found");
        model.addAttribute("errorMessage", exception.getMessage());

        return "error/404";
    }

    @ExceptionHandler(ProductNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleProductNotFound(
            ProductNotFoundException exception,
            Model model) {
        model.addAttribute(
                "errorTitle",
                "Product Not Found");

        model.addAttribute(
                "errorMessage",
                exception.getMessage());

        return "error/404";
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleCustomerNotFound(
            CustomerNotFoundException exception,
            Model model) {
        model.addAttribute(
                "errorTitle",
                "Customer Not Found");

        model.addAttribute(
                "errorMessage",
                exception.getMessage());

        return "error/404";
    }
}
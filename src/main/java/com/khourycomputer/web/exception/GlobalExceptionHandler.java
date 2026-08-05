package com.khourycomputer.web.exception;

import com.khourycomputer.domain.exception.OrderNotFoundException;
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
            Model model
    ) {
        model.addAttribute("errorTitle", "Order Not Found");
        model.addAttribute("errorMessage", exception.getMessage());

        return "error/404";
    }
}
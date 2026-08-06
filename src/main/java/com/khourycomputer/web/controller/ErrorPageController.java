package com.khourycomputer.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorPageController {

    @RequestMapping("/access-denied")
    public String showAccessDeniedPage() {
        return "error/403";
    }
}
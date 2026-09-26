package com.khourycomputer.web.controller;

import com.khourycomputer.config.security.CurrentUserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

        private final CurrentUserService currentUserService;

        public ProfileController(
                        CurrentUserService currentUserService) {
                this.currentUserService = currentUserService;
        }

        @GetMapping("/profile")
        public String showProfilePage(Model model) {
                model.addAttribute(
                                "user",
                                currentUserService.getCurrentUser());

                return "auth/profile";
        }
}
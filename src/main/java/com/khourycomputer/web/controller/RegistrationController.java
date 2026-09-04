package com.khourycomputer.web.controller;

import com.khourycomputer.application.dto.common.address.AddressRequest;
import com.khourycomputer.application.dto.user.RegisterUserRequest;
import com.khourycomputer.application.service.UserApplicationService;
import com.khourycomputer.web.viewmodel.auth.RegistrationForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

import java.util.Objects;

@Controller
public class RegistrationController {

        private final UserApplicationService userApplicationService;

        public RegistrationController(
                        UserApplicationService userApplicationService) {
                this.userApplicationService = userApplicationService;
        }

        @GetMapping("/register")
        public String showRegistrationPage(Model model) {
                if (!model.containsAttribute("registrationForm")) {
                        model.addAttribute(
                                        "registrationForm",
                                        new RegistrationForm());
                }

                return "auth/register";
        }

        @PostMapping("/register")
        public String registerCustomer(
                        @Valid @ModelAttribute("registrationForm") RegistrationForm registrationForm,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes) {
                if (!Objects.equals(
                                registrationForm.getPassword(),
                                registrationForm.getConfirmPassword())) {
                        bindingResult.rejectValue(
                                        "confirmPassword",
                                        "password.mismatch",
                                        "Passwords do not match.");
                }

                if (bindingResult.hasErrors()) {
                        return "auth/register";
                }

                try {
                        userApplicationService.registerUser(
                                        toRegisterUserRequest(registrationForm));

                        redirectAttributes.addFlashAttribute(
                                        "successMessage",
                                        "Registration completed successfully. You can now sign in.");

                        return "redirect:/login";

                } catch (IllegalArgumentException exception) {
                        if ("Email already exists.".equals(exception.getMessage())) {
                                bindingResult.rejectValue(
                                                "email",
                                                "email.duplicate",
                                                exception.getMessage());
                        } else {
                                bindingResult.reject(
                                                "registration.failed",
                                                exception.getMessage());
                        }

                        return "auth/register";
                }
        }

        private RegisterUserRequest toRegisterUserRequest(
                        RegistrationForm registrationForm) {
                AddressRequest addressRequest = new AddressRequest(
                                registrationForm.getCity(),
                                registrationForm.getStreet(),
                                registrationForm.getAddressDetails());

                return new RegisterUserRequest(
                                registrationForm.getFirstName(),
                                registrationForm.getLastName(),
                                registrationForm.getEmail(),
                                registrationForm.getPassword(),
                                registrationForm.getPhoneCountryCode(),
                                registrationForm.getPhoneNumber(),
                                addressRequest);
        }
}
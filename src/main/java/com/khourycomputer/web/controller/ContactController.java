package com.khourycomputer.web.controller;

import com.khourycomputer.application.service.StoreContactInfoApplicationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ContactController {

    private final StoreContactInfoApplicationService storeContactInfoApplicationService;

    public ContactController(
            StoreContactInfoApplicationService storeContactInfoApplicationService
    ) {
        this.storeContactInfoApplicationService =
                storeContactInfoApplicationService;
    }

    @GetMapping("/contact")
    public String showContactPage(Model model) {
        model.addAttribute(
                "contactInfo",
                storeContactInfoApplicationService.getStoreContactInfo()
        );

        return "public/contact";
    }
}
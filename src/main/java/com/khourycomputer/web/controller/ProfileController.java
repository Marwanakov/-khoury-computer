package com.khourycomputer.web.controller;

import com.khourycomputer.application.dto.common.address.AddressRequest;
import com.khourycomputer.application.dto.user.UpdateUserProfileRequest;
import com.khourycomputer.application.dto.user.UserResponse;
import com.khourycomputer.application.service.UserApplicationService;
import com.khourycomputer.config.security.CurrentUserService;
import com.khourycomputer.web.viewmodel.profile.ProfileForm;
import com.khourycomputer.domain.model.PalestinianPhoneNumber;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

        private final CurrentUserService currentUserService;
        private final UserApplicationService userApplicationService;

        public ProfileController(
                        CurrentUserService currentUserService,
                        UserApplicationService userApplicationService) {
                this.currentUserService = currentUserService;
                this.userApplicationService = userApplicationService;
        }

        @GetMapping("/profile")
        public String showProfilePage(Model model) {
                model.addAttribute(
                                "user",
                                currentUserService.getCurrentUser());

                return "auth/profile";
        }

        @GetMapping("/profile/edit")
        public String showEditProfilePage(Model model) {
                UserResponse currentUser = currentUserService.getCurrentUser();

                model.addAttribute(
                                "profileForm",
                                toProfileForm(currentUser));

                return "auth/profile-edit";
        }

        @PostMapping("/profile/edit")
        public String updateProfile(
                        @Valid @ModelAttribute("profileForm") ProfileForm profileForm,
                        BindingResult bindingResult,
                        HttpServletRequest request,
                        RedirectAttributes redirectAttributes) throws ServletException {

                if (bindingResult.hasErrors()) {
                        return "auth/profile-edit";
                }

                UserResponse currentUser = currentUserService.getCurrentUser();

                try {
                        UserResponse updatedUser = userApplicationService.updateUserProfile(
                                        currentUser.id(),
                                        toUpdateUserProfileRequest(profileForm));

                        boolean emailChanged = !currentUser.email().equalsIgnoreCase(
                                        updatedUser.email());

                        if (emailChanged) {
                                request.logout();

                                return "redirect:/login?emailChanged";
                        }

                        redirectAttributes.addFlashAttribute(
                                        "successMessage",
                                        "Your profile was updated successfully.");

                        return "redirect:/profile";

                } catch (IllegalArgumentException exception) {
                        if ("Email already exists.".equals(exception.getMessage())) {
                                bindingResult.rejectValue(
                                                "email",
                                                "email.duplicate",
                                                exception.getMessage());
                        } else {
                                bindingResult.reject(
                                                "profile.update.failed",
                                                exception.getMessage());
                        }

                        return "auth/profile-edit";
                }
        }

        private ProfileForm toProfileForm(UserResponse user) {
                ProfileForm profileForm = new ProfileForm();

                profileForm.setFirstName(user.firstName());
                profileForm.setLastName(user.lastName());
                profileForm.setEmail(user.email());
                profileForm.setCity(user.address().city());
                profileForm.setStreet(user.address().street());
                profileForm.setAddressDetails(
                                user.address().details());

                populatePhoneFields(
                                profileForm,
                                user.phoneNumber());

                return profileForm;
        }

        private UpdateUserProfileRequest toUpdateUserProfileRequest(
                        ProfileForm profileForm) {
                AddressRequest addressRequest = new AddressRequest(
                                profileForm.getCity(),
                                profileForm.getStreet(),
                                profileForm.getAddressDetails());

                return new UpdateUserProfileRequest(
                                profileForm.getFirstName(),
                                profileForm.getLastName(),
                                profileForm.getEmail(),
                                profileForm.getPhoneCountryCode(),
                                profileForm.getPhoneNumber(),
                                addressRequest);
        }

        private void populatePhoneFields(
                        ProfileForm profileForm,
                        String storedPhoneNumber) {
                try {
                        PalestinianPhoneNumber phoneNumber = PalestinianPhoneNumber
                                        .fromInternationalNumber(
                                                        storedPhoneNumber);

                        profileForm.setPhoneCountryCode(
                                        phoneNumber.getCountryCode());

                        profileForm.setPhoneNumber(
                                        phoneNumber.getLocalNumber());

                } catch (IllegalArgumentException exception) {
                        // Compatibility for accounts created before normalized
                        // international phone-number storage was introduced.
                        profileForm.setPhoneCountryCode(
                                        PalestinianPhoneNumber.PALESTINE_COUNTRY_CODE);

                        profileForm.setPhoneNumber(
                                        storedPhoneNumber);
                }
        }
}
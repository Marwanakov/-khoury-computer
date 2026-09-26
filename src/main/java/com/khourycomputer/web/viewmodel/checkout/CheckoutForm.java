package com.khourycomputer.web.viewmodel.checkout;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CheckoutForm {

    @NotBlank(message = "Country code is required.")
    @Pattern(
            regexp = "^\\+(970|972)$",
            message = "Country code must be +970 or +972."
    )
    private String phoneCountryCode = "+970";

    @NotBlank(message = "Phone number is required.")
    @Size(
            max = 20,
            message = "Phone number cannot exceed 20 characters."
    )
    @Pattern(
            regexp = "^0?5[0-9 ()-]*$",
            message = "Enter a mobile number starting with 05 or 5."
    )
    private String phoneNumber;

    @NotBlank(message = "City is required.")
    @Size(
            min = 2,
            max = 100,
            message = "City must be between 2 and 100 characters."
    )
    @Pattern(
            regexp = "^[\\p{L}\\p{M}][\\p{L}\\p{M}' -]*$",
            message = "City may contain only letters, spaces, apostrophes, and hyphens."
    )
    private String city;

    @NotBlank(message = "Street is required.")
    @Size(
            min = 2,
            max = 150,
            message = "Street must be between 2 and 150 characters."
    )
    @Pattern(
            regexp = "^[\\p{L}\\p{M}0-9 .,'/#()-]+$",
            message = "Street contains unsupported characters."
    )
    private String street;

    @Size(
            max = 300,
            message = "Address details cannot exceed 300 characters."
    )
    private String addressDetails;

    public String getPhoneCountryCode() {
        return phoneCountryCode;
    }

    public void setPhoneCountryCode(String phoneCountryCode) {
        this.phoneCountryCode = phoneCountryCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getAddressDetails() {
        return addressDetails;
    }

    public void setAddressDetails(String addressDetails) {
        this.addressDetails = addressDetails;
    }
}
package com.khourycomputer.web.viewmodel.profile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ProfileForm {

    @NotBlank(message = "First name is required.")
    @Size(
            min = 2,
            max = 50,
            message = "First name must be between 2 and 50 characters."
    )
    @Pattern(
            regexp = "^[\\p{L}\\p{M}][\\p{L}\\p{M}' -]*$",
            message = "First name may contain only letters, spaces, apostrophes, and hyphens."
    )
    private String firstName;

    @NotBlank(message = "Last name is required.")
    @Size(
            min = 2,
            max = 50,
            message = "Last name must be between 2 and 50 characters."
    )
    @Pattern(
            regexp = "^[\\p{L}\\p{M}][\\p{L}\\p{M}' -]*$",
            message = "Last name may contain only letters, spaces, apostrophes, and hyphens."
    )
    private String lastName;

    @NotBlank(message = "Email is required.")
    @Email(
            message = "Enter a valid email address, such as name@example.com."
    )
    @Size(
            max = 150,
            message = "Email cannot exceed 150 characters."
    )
    private String email;

    @NotBlank(message = "Phone number is required.")
    @Pattern(
            regexp = "^\\+?[0-9][0-9 ()-]{6,19}$",
            message = "Enter a valid phone number using 7 to 20 characters."
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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
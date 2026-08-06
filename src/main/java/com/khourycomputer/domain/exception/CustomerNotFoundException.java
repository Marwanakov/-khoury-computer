package com.khourycomputer.domain.exception;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(Long customerId) {
        super(
                "Customer with ID "
                        + customerId
                        + " was not found."
        );
    }
}
package com.khourycomputer.domain.exception;

public class ProductDealNotFoundException
        extends RuntimeException {

    public ProductDealNotFoundException(Long dealId) {
        super("Product deal with ID "
                + dealId
                + " was not found.");
    }
}
package com.auction.platform.service.customer;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(Long id) {
        super("Customer not found with id: " + id);
    }
}

package com.auction.platform.service.account;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(Long customerId) {
        super("Account not found for customer id: " + customerId);
    }
}

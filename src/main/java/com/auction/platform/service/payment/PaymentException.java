package com.auction.platform.service.payment;

public class PaymentException extends RuntimeException {

    public PaymentException(String message) {
        super(message);
    }
}

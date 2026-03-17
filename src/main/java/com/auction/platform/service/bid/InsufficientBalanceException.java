package com.auction.platform.service.bid;

import java.math.BigDecimal;

public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(BigDecimal required, BigDecimal available) {
        super("Insufficient balance: required " + required + ", available " + available);
    }
}

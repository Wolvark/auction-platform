package com.auction.platform.service.bid;

public class BidNotFoundException extends RuntimeException {

    public BidNotFoundException(Long id) {
        super("Bid not found with id: " + id);
    }
}

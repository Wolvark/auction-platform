package com.auction.platform.bid;

public class BidNotFoundException extends RuntimeException {

    public BidNotFoundException(Long id) {
        super("Bid not found with id: " + id);
    }
}

package com.auction.platform.service.auction;

public class AuctionNotFoundException extends RuntimeException {

    public AuctionNotFoundException(Long id) {
        super("Auction not found with id: " + id);
    }
}

package com.auction.platform.auction;

public class AuctionNotFoundException extends RuntimeException {

    public AuctionNotFoundException(Long id) {
        super("Auction not found with id: " + id);
    }
}

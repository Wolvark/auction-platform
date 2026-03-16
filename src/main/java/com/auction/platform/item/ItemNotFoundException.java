package com.auction.platform.item;

public class ItemNotFoundException extends RuntimeException {

    public ItemNotFoundException(Long id) {
        super("Item not found with id: " + id);
    }
}

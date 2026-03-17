package com.auction.platform.service.item;

public class ItemMediaLinkNotFoundException extends RuntimeException {

    public ItemMediaLinkNotFoundException(Long id) {
        super("Item media link not found with id: " + id);
    }
}

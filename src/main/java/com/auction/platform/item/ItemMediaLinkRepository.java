package com.auction.platform.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemMediaLinkRepository extends JpaRepository<ItemMediaLink, Long> {

    List<ItemMediaLink> findByItemIdOrderByDisplayOrderAsc(Long itemId);
}

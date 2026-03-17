package com.auction.platform.repository.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import com.auction.platform.model.item.ItemMediaLink;

@Repository
public interface ItemMediaLinkRepository extends JpaRepository<ItemMediaLink, Long> {

    List<ItemMediaLink> findByItemIdOrderByDisplayOrderAsc(Long itemId);
}

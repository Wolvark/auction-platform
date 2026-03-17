package com.auction.platform.repository.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import com.auction.platform.model.item.Item;
import com.auction.platform.model.item.ItemCategory;
import com.auction.platform.model.item.ItemStatus;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByOwnerId(Long ownerId);

    List<Item> findByStatus(ItemStatus status);

    List<Item> findByCategory(ItemCategory category);
}

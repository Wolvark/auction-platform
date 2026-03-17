package com.auction.platform.controller.item;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import com.auction.platform.model.item.ItemCategory;
import com.auction.platform.model.item.ItemCondition;
import com.auction.platform.model.item.ItemStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemResponse {

    private Long id;
    private String title;
    private String description;
    private ItemCategory category;
    private ItemCondition condition;
    private ItemStatus status;
    private Long ownerId;
    private String ownerEmail;
    private List<ItemMediaLinkResponse> mediaLinks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.auction.platform.item;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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

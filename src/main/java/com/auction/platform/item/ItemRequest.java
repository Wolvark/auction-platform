package com.auction.platform.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Category is required")
    private ItemCategory category;

    @NotNull(message = "Condition is required")
    private ItemCondition condition;

    @NotNull(message = "Owner ID is required")
    private Long ownerId;

    @Valid
    private List<ItemMediaLinkRequest> mediaLinks;
}

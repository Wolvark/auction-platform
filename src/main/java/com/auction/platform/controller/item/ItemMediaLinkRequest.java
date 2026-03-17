package com.auction.platform.controller.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import com.auction.platform.model.item.MediaType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemMediaLinkRequest {

    @NotBlank(message = "URL is required")
    private String url;

    @NotNull(message = "Media type is required")
    private MediaType mediaType;

    private Integer displayOrder;
}

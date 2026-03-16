package com.auction.platform.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

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

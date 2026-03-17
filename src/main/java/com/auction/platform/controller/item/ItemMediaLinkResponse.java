package com.auction.platform.controller.item;

import lombok.*;
import com.auction.platform.model.item.MediaType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemMediaLinkResponse {

    private Long id;
    private String url;
    private MediaType mediaType;
    private Integer displayOrder;
}

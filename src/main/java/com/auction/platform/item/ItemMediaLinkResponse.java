package com.auction.platform.item;

import lombok.*;

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

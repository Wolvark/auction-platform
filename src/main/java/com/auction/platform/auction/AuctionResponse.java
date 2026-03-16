package com.auction.platform.auction;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionResponse {

    private Long id;
    private Long itemId;
    private String itemTitle;
    private BigDecimal startPrice;
    private BigDecimal reservePrice;
    private BigDecimal buyOutPrice;
    private BigDecimal minBidIncrement;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private AuctionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.auction.platform.controller.bid;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.auction.platform.model.bid.BidStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BidResponse {

    private Long id;
    private String itemName;
    private BigDecimal amount;
    private Long customerId;
    private String customerEmail;
    private Long auctionId;
    private BidStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

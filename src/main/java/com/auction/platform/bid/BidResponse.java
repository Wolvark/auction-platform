package com.auction.platform.bid;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private BidStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

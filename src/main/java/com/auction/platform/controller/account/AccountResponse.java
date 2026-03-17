package com.auction.platform.controller.account;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponse {

    private Long id;
    private Long customerId;
    private String username;
    private BigDecimal balance;
    private BigDecimal heldAmount;
    private BigDecimal availableBalance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

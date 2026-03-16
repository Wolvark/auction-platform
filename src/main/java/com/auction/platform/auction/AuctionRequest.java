package com.auction.platform.auction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionRequest {

    @NotNull(message = "Item ID is required")
    private Long itemId;

    @NotNull(message = "Start price is required")
    @DecimalMin(value = "0.01", message = "Start price must be greater than 0")
    private BigDecimal startPrice;

    @DecimalMin(value = "0.01", message = "Reserve price must be greater than 0")
    private BigDecimal reservePrice;

    @DecimalMin(value = "0.01", message = "Buy-out price must be greater than 0")
    private BigDecimal buyOutPrice;

    @DecimalMin(value = "0.01", message = "Minimum bid increment must be greater than 0")
    private BigDecimal minBidIncrement;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;
}

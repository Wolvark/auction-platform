package com.auction.platform.controller.account;

import com.auction.platform.model.account.PaymentMethod;
import com.auction.platform.model.account.TransactionStatus;
import com.auction.platform.model.account.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    private Long id;
    private BigDecimal amount;
    private TransactionType type;
    private PaymentMethod paymentMethod;
    private TransactionStatus status;
    private String referenceId;
    private String description;
    private LocalDateTime createdAt;
}

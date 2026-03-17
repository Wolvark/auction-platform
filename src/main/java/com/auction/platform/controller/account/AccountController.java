package com.auction.platform.controller.account;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.auction.platform.controller.bid.BidResponse;
import com.auction.platform.model.customer.Customer;
import com.auction.platform.service.account.AccountService;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/me")
    public ResponseEntity<AccountResponse> getMyAccount(@AuthenticationPrincipal Customer customer) {
        return ResponseEntity.ok(accountService.getAccount(customer));
    }

    @PostMapping("/me/deposit")
    public ResponseEntity<AccountResponse> deposit(
            @AuthenticationPrincipal Customer customer,
            @Valid @RequestBody DepositRequest request) {
        return ResponseEntity.ok(accountService.deposit(customer, request));
    }

    @GetMapping("/me/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactionHistory(
            @AuthenticationPrincipal Customer customer) {
        return ResponseEntity.ok(accountService.getTransactionHistory(customer));
    }

    @GetMapping("/me/bids")
    public ResponseEntity<List<BidResponse>> getBidHistory(
            @AuthenticationPrincipal Customer customer) {
        return ResponseEntity.ok(accountService.getBidHistory(customer));
    }
}

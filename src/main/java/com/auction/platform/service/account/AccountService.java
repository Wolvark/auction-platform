package com.auction.platform.service.account;

import com.auction.platform.controller.account.AccountResponse;
import com.auction.platform.controller.account.DepositRequest;
import com.auction.platform.controller.account.TransactionResponse;
import com.auction.platform.controller.bid.BidResponse;
import com.auction.platform.model.account.Account;
import com.auction.platform.model.account.PaymentMethod;
import com.auction.platform.model.account.Transaction;
import com.auction.platform.model.account.TransactionStatus;
import com.auction.platform.model.account.TransactionType;
import com.auction.platform.model.customer.Customer;
import com.auction.platform.repository.account.AccountRepository;
import com.auction.platform.repository.account.TransactionRepository;
import com.auction.platform.repository.bid.BidRepository;
import com.auction.platform.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final BidRepository bidRepository;
    private final PaymentService paymentService;

    public AccountResponse getAccount(Customer customer) {
        Account account = accountRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new AccountNotFoundException(customer.getId()));
        return toResponse(account);
    }

    @Transactional
    public AccountResponse deposit(Customer customer, DepositRequest request) {
        Account account = accountRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new AccountNotFoundException(customer.getId()));

        String referenceId = paymentService.processDeposit(request.getAmount(), request.getPaymentMethod());

        Transaction transaction = Transaction.builder()
                .account(account)
                .amount(request.getAmount())
                .type(TransactionType.DEPOSIT)
                .paymentMethod(request.getPaymentMethod())
                .status(TransactionStatus.COMPLETED)
                .referenceId(referenceId)
                .description("Deposit via " + formatPaymentMethod(request.getPaymentMethod()))
                .build();

        transactionRepository.save(transaction);
        account.setBalance(account.getBalance().add(request.getAmount()));
        return toResponse(accountRepository.save(account));
    }

    public List<TransactionResponse> getTransactionHistory(Customer customer) {
        Account account = accountRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new AccountNotFoundException(customer.getId()));
        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(account.getId())
                .stream()
                .map(this::toTransactionResponse)
                .toList();
    }

    public List<BidResponse> getBidHistory(Customer customer) {
        return bidRepository.findByCustomerId(customer.getId())
                .stream()
                .map(bid -> BidResponse.builder()
                        .id(bid.getId())
                        .itemName(bid.getItemName())
                        .amount(bid.getAmount())
                        .customerId(bid.getCustomer().getId())
                        .customerEmail(bid.getCustomer().getEmail())
                        .status(bid.getStatus())
                        .createdAt(bid.getCreatedAt())
                        .updatedAt(bid.getUpdatedAt())
                        .build())
                .toList();
    }

    private String formatPaymentMethod(PaymentMethod method) {
        return switch (method) {
            case VISA -> "Visa";
            case MASTERCARD -> "Mastercard";
            case GOOGLE_PAY -> "Google Pay";
            case APPLE_PAY -> "Apple Pay";
            case INTERNAL -> "Internal";
        };
    }

    private AccountResponse toResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .customerId(account.getCustomer().getId())
                .username(account.getCustomer().getUsername())
                .balance(account.getBalance())
                .heldAmount(account.getHeldAmount())
                .availableBalance(account.getBalance().subtract(account.getHeldAmount()))
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    private TransactionResponse toTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .paymentMethod(transaction.getPaymentMethod())
                .status(transaction.getStatus())
                .referenceId(transaction.getReferenceId())
                .description(transaction.getDescription())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}

package com.auction.platform.service.bid;

import com.auction.platform.model.account.Account;
import com.auction.platform.model.customer.Customer;
import com.auction.platform.repository.account.AccountRepository;
import com.auction.platform.service.account.AccountNotFoundException;
import com.auction.platform.service.customer.CustomerNotFoundException;
import com.auction.platform.repository.customer.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import com.auction.platform.controller.bid.BidRequest;
import com.auction.platform.controller.bid.BidResponse;
import com.auction.platform.model.bid.Bid;
import com.auction.platform.model.bid.BidStatus;
import com.auction.platform.repository.bid.BidRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BidService {

    private final BidRepository bidRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public List<BidResponse> findAll() {
        return bidRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public BidResponse findById(Long id) {
        return bidRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new BidNotFoundException(id));
    }

    public List<BidResponse> findByCustomerId(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
        return bidRepository.findByCustomer(customer)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<BidResponse> findByStatus(BidStatus status) {
        return bidRepository.findByStatus(status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public BidResponse create(BidRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException(request.getCustomerId()));

        // Lock account row for update to prevent concurrent balance issues
        Account account = accountRepository.findByCustomerIdForUpdate(customer.getId())
                .orElseThrow(() -> new AccountNotFoundException(customer.getId()));

        BigDecimal available = account.getBalance().subtract(account.getHeldAmount());
        if (available.compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(request.getAmount(), available);
        }

        // Outbid previous active bidders on this auction and return their held amounts
        List<Bid> activeBids = bidRepository.findByAuctionIdAndStatus(request.getAuctionId(), BidStatus.ACTIVE);
        for (Bid previousBid : activeBids) {
            Account previousAccount = accountRepository.findByCustomerIdForUpdate(
                    previousBid.getCustomer().getId())
                    .orElseThrow(() -> new AccountNotFoundException(previousBid.getCustomer().getId()));
            previousAccount.setHeldAmount(
                    previousAccount.getHeldAmount().subtract(previousBid.getAmount()));
            accountRepository.save(previousAccount);
            previousBid.setStatus(BidStatus.OUTBID);
            bidRepository.save(previousBid);
        }

        // Hold the bid amount in the customer's account
        account.setHeldAmount(account.getHeldAmount().add(request.getAmount()));
        accountRepository.save(account);

        Bid bid = Bid.builder()
                .itemName(request.getItemName())
                .amount(request.getAmount())
                .customer(customer)
                .auctionId(request.getAuctionId())
                .status(BidStatus.ACTIVE)
                .build();

        BidResponse response = toResponse(bidRepository.save(bid));

        // Broadcast to WebSocket subscribers for live updates
        messagingTemplate.convertAndSend(
                "/topic/auction/" + request.getAuctionId() + "/bids", response);

        return response;
    }

    @Transactional
    public BidResponse updateStatus(Long id, BidStatus status) {
        Bid bid = bidRepository.findById(id)
                .orElseThrow(() -> new BidNotFoundException(id));
        bid.setStatus(status);
        return toResponse(bidRepository.save(bid));
    }

    @Transactional
    public void delete(Long id) {
        if (!bidRepository.existsById(id)) {
            throw new BidNotFoundException(id);
        }
        bidRepository.deleteById(id);
    }

    private BidResponse toResponse(Bid bid) {
        return BidResponse.builder()
                .id(bid.getId())
                .itemName(bid.getItemName())
                .amount(bid.getAmount())
                .customerId(bid.getCustomer().getId())
                .customerEmail(bid.getCustomer().getEmail())
                .auctionId(bid.getAuctionId())
                .status(bid.getStatus())
                .createdAt(bid.getCreatedAt())
                .updatedAt(bid.getUpdatedAt())
                .build();
    }
}

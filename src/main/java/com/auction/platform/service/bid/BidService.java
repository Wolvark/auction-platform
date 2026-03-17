package com.auction.platform.service.bid;

import com.auction.platform.model.customer.Customer;
import com.auction.platform.service.customer.CustomerNotFoundException;
import com.auction.platform.repository.customer.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        Bid bid = Bid.builder()
                .itemName(request.getItemName())
                .amount(request.getAmount())
                .customer(customer)
                .status(BidStatus.PENDING)
                .build();
        return toResponse(bidRepository.save(bid));
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
                .status(bid.getStatus())
                .createdAt(bid.getCreatedAt())
                .updatedAt(bid.getUpdatedAt())
                .build();
    }
}

package com.auction.platform.controller.bid;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.auction.platform.model.bid.BidStatus;
import com.auction.platform.service.bid.BidService;

@RestController
@RequestMapping("/api/v1/bids")
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;

    @GetMapping
    public ResponseEntity<List<BidResponse>> getAll() {
        return ResponseEntity.ok(bidService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BidResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bidService.findById(id));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<BidResponse>> getByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(bidService.findByCustomerId(customerId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<BidResponse>> getByStatus(@PathVariable BidStatus status) {
        return ResponseEntity.ok(bidService.findByStatus(status));
    }

    @PostMapping
    public ResponseEntity<BidResponse> create(@Valid @RequestBody BidRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bidService.create(request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<BidResponse> updateStatus(@PathVariable Long id,
                                                    @RequestParam BidStatus status) {
        return ResponseEntity.ok(bidService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bidService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

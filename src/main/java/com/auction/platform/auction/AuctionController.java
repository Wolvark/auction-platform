package com.auction.platform.auction;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auctions")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;

    @GetMapping
    public ResponseEntity<List<AuctionResponse>> getAll() {
        return ResponseEntity.ok(auctionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuctionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(auctionService.findById(id));
    }

    @GetMapping("/item/{itemId}")
    public ResponseEntity<List<AuctionResponse>> getByItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(auctionService.findByItemId(itemId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<AuctionResponse>> getByStatus(@PathVariable AuctionStatus status) {
        return ResponseEntity.ok(auctionService.findByStatus(status));
    }

    @PostMapping
    public ResponseEntity<AuctionResponse> create(@Valid @RequestBody AuctionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(auctionService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuctionResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody AuctionRequest request) {
        return ResponseEntity.ok(auctionService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AuctionResponse> updateStatus(@PathVariable Long id,
                                                         @RequestParam AuctionStatus status) {
        return ResponseEntity.ok(auctionService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        auctionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

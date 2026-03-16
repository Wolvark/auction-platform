package com.auction.platform.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<List<ItemResponse>> getAll() {
        return ResponseEntity.ok(itemService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.findById(id));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<ItemResponse>> getByOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(itemService.findByOwnerId(ownerId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ItemResponse>> getByStatus(@PathVariable ItemStatus status) {
        return ResponseEntity.ok(itemService.findByStatus(status));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ItemResponse>> getByCategory(@PathVariable ItemCategory category) {
        return ResponseEntity.ok(itemService.findByCategory(category));
    }

    @PostMapping
    public ResponseEntity<ItemResponse> create(@Valid @RequestBody ItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody ItemRequest request) {
        return ResponseEntity.ok(itemService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ItemResponse> updateStatus(@PathVariable Long id,
                                                     @RequestParam ItemStatus status) {
        return ResponseEntity.ok(itemService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        itemService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/media")
    public ResponseEntity<ItemResponse> addMediaLink(@PathVariable Long id,
                                                     @Valid @RequestBody ItemMediaLinkRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.addMediaLink(id, request));
    }

    @DeleteMapping("/{id}/media/{mediaId}")
    public ResponseEntity<Void> removeMediaLink(@PathVariable Long id,
                                                @PathVariable Long mediaId) {
        itemService.removeMediaLink(id, mediaId);
        return ResponseEntity.noContent().build();
    }
}

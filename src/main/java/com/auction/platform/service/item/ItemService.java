package com.auction.platform.service.item;

import com.auction.platform.model.customer.Customer;
import com.auction.platform.service.customer.CustomerNotFoundException;
import com.auction.platform.repository.customer.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import com.auction.platform.controller.item.ItemMediaLinkRequest;
import com.auction.platform.controller.item.ItemMediaLinkResponse;
import com.auction.platform.controller.item.ItemRequest;
import com.auction.platform.controller.item.ItemResponse;
import com.auction.platform.model.item.Item;
import com.auction.platform.model.item.ItemCategory;
import com.auction.platform.model.item.ItemMediaLink;
import com.auction.platform.model.item.ItemStatus;
import com.auction.platform.repository.item.ItemMediaLinkRepository;
import com.auction.platform.repository.item.ItemRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemMediaLinkRepository itemMediaLinkRepository;
    private final CustomerRepository customerRepository;

    public List<ItemResponse> findAll() {
        return itemRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ItemResponse findById(Long id) {
        return itemRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ItemNotFoundException(id));
    }

    public List<ItemResponse> findByOwnerId(Long ownerId) {
        if (!customerRepository.existsById(ownerId)) {
            throw new CustomerNotFoundException(ownerId);
        }
        return itemRepository.findByOwnerId(ownerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ItemResponse> findByStatus(ItemStatus status) {
        return itemRepository.findByStatus(status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ItemResponse> findByCategory(ItemCategory category) {
        return itemRepository.findByCategory(category)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ItemResponse create(ItemRequest request) {
        Customer owner = customerRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new CustomerNotFoundException(request.getOwnerId()));

        Item item = Item.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .condition(request.getCondition())
                .status(ItemStatus.DRAFT)
                .owner(owner)
                .build();

        if (request.getMediaLinks() != null) {
            for (ItemMediaLinkRequest mediaRequest : request.getMediaLinks()) {
                ItemMediaLink link = ItemMediaLink.builder()
                        .item(item)
                        .url(mediaRequest.getUrl())
                        .mediaType(mediaRequest.getMediaType())
                        .displayOrder(Objects.requireNonNullElse(mediaRequest.getDisplayOrder(), 0))
                        .build();
                item.getMediaLinks().add(link);
            }
        }

        return toResponse(itemRepository.save(item));
    }

    @Transactional
    public ItemResponse update(Long id, ItemRequest request) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));

        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setCategory(request.getCategory());
        item.setCondition(request.getCondition());

        return toResponse(itemRepository.save(item));
    }

    @Transactional
    public ItemResponse updateStatus(Long id, ItemStatus status) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));
        item.setStatus(status);
        return toResponse(itemRepository.save(item));
    }

    @Transactional
    public void delete(Long id) {
        if (!itemRepository.existsById(id)) {
            throw new ItemNotFoundException(id);
        }
        itemRepository.deleteById(id);
    }

    @Transactional
    public ItemResponse addMediaLink(Long itemId, ItemMediaLinkRequest request) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));

        ItemMediaLink link = ItemMediaLink.builder()
                .item(item)
                .url(request.getUrl())
                .mediaType(request.getMediaType())
                .displayOrder(Objects.requireNonNullElse(request.getDisplayOrder(), 0))
                .build();
        item.getMediaLinks().add(link);

        return toResponse(itemRepository.save(item));
    }

    @Transactional
    public void removeMediaLink(Long itemId, Long mediaLinkId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));

        ItemMediaLink link = itemMediaLinkRepository.findById(mediaLinkId)
                .orElseThrow(() -> new ItemMediaLinkNotFoundException(mediaLinkId));

        if (!link.getItem().getId().equals(itemId)) {
            throw new IllegalArgumentException(
                    "Media link " + mediaLinkId + " does not belong to item " + itemId);
        }

        item.getMediaLinks().remove(link);
        itemMediaLinkRepository.delete(link);
    }

    private ItemResponse toResponse(Item item) {
        List<ItemMediaLinkResponse> mediaResponses = item.getMediaLinks()
                .stream()
                .map(m -> ItemMediaLinkResponse.builder()
                        .id(m.getId())
                        .url(m.getUrl())
                        .mediaType(m.getMediaType())
                        .displayOrder(m.getDisplayOrder())
                        .build())
                .toList();

        return ItemResponse.builder()
                .id(item.getId())
                .title(item.getTitle())
                .description(item.getDescription())
                .category(item.getCategory())
                .condition(item.getCondition())
                .status(item.getStatus())
                .ownerId(item.getOwner().getId())
                .ownerEmail(item.getOwner().getEmail())
                .mediaLinks(mediaResponses)
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}

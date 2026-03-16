package com.auction.platform.auction;

import com.auction.platform.item.Item;
import com.auction.platform.item.ItemNotFoundException;
import com.auction.platform.item.ItemRepository;
import com.auction.platform.item.ItemStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final ItemRepository itemRepository;

    public List<AuctionResponse> findAll() {
        return auctionRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public AuctionResponse findById(Long id) {
        return auctionRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new AuctionNotFoundException(id));
    }

    public List<AuctionResponse> findByItemId(Long itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new ItemNotFoundException(itemId);
        }
        return auctionRepository.findByItemId(itemId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<AuctionResponse> findByStatus(AuctionStatus status) {
        return auctionRepository.findByStatus(status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AuctionResponse create(AuctionRequest request) {
        if (request.getEndTime().isBefore(request.getStartTime()) ||
                request.getEndTime().isEqual(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ItemNotFoundException(request.getItemId()));

        auctionRepository.findFirstByItemIdAndStatusNot(item.getId(), AuctionStatus.CANCELLED)
                .ifPresent(existing -> {
                    if (existing.getStatus() != AuctionStatus.ENDED) {
                        throw new IllegalArgumentException(
                                "Item already has an active or scheduled auction");
                    }
                });

        Auction auction = Auction.builder()
                .item(item)
                .startPrice(request.getStartPrice())
                .reservePrice(request.getReservePrice())
                .buyOutPrice(request.getBuyOutPrice())
                .minBidIncrement(Objects.requireNonNullElse(request.getMinBidIncrement(), BigDecimal.ONE))
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(AuctionStatus.SCHEDULED)
                .build();

        item.setStatus(ItemStatus.READY);
        itemRepository.save(item);

        return toResponse(auctionRepository.save(auction));
    }

    @Transactional
    public AuctionResponse update(Long id, AuctionRequest request) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new AuctionNotFoundException(id));

        if (auction.getStatus() == AuctionStatus.ACTIVE ||
                auction.getStatus() == AuctionStatus.ENDED) {
            throw new IllegalArgumentException(
                    "Cannot update an auction that is active or ended");
        }

        if (request.getEndTime().isBefore(request.getStartTime()) ||
                request.getEndTime().isEqual(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        auction.setStartPrice(request.getStartPrice());
        auction.setReservePrice(request.getReservePrice());
        auction.setBuyOutPrice(request.getBuyOutPrice());
        auction.setMinBidIncrement(
                Objects.requireNonNullElse(request.getMinBidIncrement(), BigDecimal.ONE));
        auction.setStartTime(request.getStartTime());
        auction.setEndTime(request.getEndTime());

        return toResponse(auctionRepository.save(auction));
    }

    @Transactional
    public AuctionResponse updateStatus(Long id, AuctionStatus status) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new AuctionNotFoundException(id));
        auction.setStatus(status);

        if (status == AuctionStatus.ACTIVE) {
            auction.getItem().setStatus(ItemStatus.AUCTIONING);
            itemRepository.save(auction.getItem());
        } else if (status == AuctionStatus.ENDED || status == AuctionStatus.CANCELLED) {
            auction.getItem().setStatus(ItemStatus.READY);
            itemRepository.save(auction.getItem());
        }

        return toResponse(auctionRepository.save(auction));
    }

    @Transactional
    public void delete(Long id) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new AuctionNotFoundException(id));

        if (auction.getStatus() == AuctionStatus.ACTIVE) {
            throw new IllegalArgumentException("Cannot delete an active auction");
        }

        auctionRepository.deleteById(id);
    }

    private AuctionResponse toResponse(Auction auction) {
        return AuctionResponse.builder()
                .id(auction.getId())
                .itemId(auction.getItem().getId())
                .itemTitle(auction.getItem().getTitle())
                .startPrice(auction.getStartPrice())
                .reservePrice(auction.getReservePrice())
                .buyOutPrice(auction.getBuyOutPrice())
                .minBidIncrement(auction.getMinBidIncrement())
                .startTime(auction.getStartTime())
                .endTime(auction.getEndTime())
                .status(auction.getStatus())
                .createdAt(auction.getCreatedAt())
                .updatedAt(auction.getUpdatedAt())
                .build();
    }
}

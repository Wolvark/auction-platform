package com.auction.platform.auction;

import com.auction.platform.customer.Customer;
import com.auction.platform.item.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionServiceTest {

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private AuctionService auctionService;

    private Customer buildCustomer(Long id) {
        Customer c = new Customer();
        c.setId(id);
        c.setFirstName("Jane");
        c.setLastName("Smith");
        c.setEmail("jane@example.com");
        c.setCreatedAt(LocalDateTime.now());
        c.setUpdatedAt(LocalDateTime.now());
        return c;
    }

    private Item buildItem(Long id) {
        Item item = new Item();
        item.setId(id);
        item.setTitle("Vintage Camera");
        item.setCategory(ItemCategory.ELECTRONICS);
        item.setCondition(ItemCondition.GOOD);
        item.setStatus(ItemStatus.DRAFT);
        item.setOwner(buildCustomer(1L));
        item.setMediaLinks(new ArrayList<>());
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        return item;
    }

    private Auction buildAuction(Long id, Item item) {
        Auction a = new Auction();
        a.setId(id);
        a.setItem(item);
        a.setStartPrice(new BigDecimal("100.00"));
        a.setMinBidIncrement(BigDecimal.ONE);
        a.setStartTime(LocalDateTime.now().plusDays(1));
        a.setEndTime(LocalDateTime.now().plusDays(7));
        a.setStatus(AuctionStatus.SCHEDULED);
        a.setCreatedAt(LocalDateTime.now());
        a.setUpdatedAt(LocalDateTime.now());
        return a;
    }

    private AuctionRequest buildRequest(Long itemId) {
        return AuctionRequest.builder()
                .itemId(itemId)
                .startPrice(new BigDecimal("100.00"))
                .minBidIncrement(new BigDecimal("5.00"))
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(7))
                .build();
    }

    @Test
    void findAll_returnsAllAuctions() {
        Item item = buildItem(1L);
        when(auctionRepository.findAll()).thenReturn(List.of(buildAuction(1L, item)));
        List<AuctionResponse> result = auctionService.findAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getItemTitle()).isEqualTo("Vintage Camera");
    }

    @Test
    void findById_existingId_returnsAuction() {
        Item item = buildItem(1L);
        Auction auction = buildAuction(1L, item);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        AuctionResponse response = auctionService.findById(1L);
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void findById_missingId_throwsNotFoundException() {
        when(auctionRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> auctionService.findById(99L))
                .isInstanceOf(AuctionNotFoundException.class);
    }

    @Test
    void findByItemId_unknownItem_throwsItemNotFound() {
        when(itemRepository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> auctionService.findByItemId(99L))
                .isInstanceOf(ItemNotFoundException.class);
    }

    @Test
    void create_validRequest_savesAndReturnsAuction() {
        Item item = buildItem(1L);
        AuctionRequest request = buildRequest(1L);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(auctionRepository.findFirstByItemIdAndStatusNot(1L, AuctionStatus.CANCELLED))
                .thenReturn(Optional.empty());
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        Auction saved = buildAuction(1L, item);
        when(auctionRepository.save(any(Auction.class))).thenReturn(saved);

        AuctionResponse response = auctionService.create(request);
        assertThat(response.getStartPrice()).isEqualByComparingTo("100.00");
        assertThat(response.getStatus()).isEqualTo(AuctionStatus.SCHEDULED);
    }

    @Test
    void create_endTimeBeforeStartTime_throwsIllegalArgument() {
        AuctionRequest request = AuctionRequest.builder()
                .itemId(1L)
                .startPrice(new BigDecimal("100.00"))
                .startTime(LocalDateTime.now().plusDays(7))
                .endTime(LocalDateTime.now().plusDays(1))
                .build();

        assertThatThrownBy(() -> auctionService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("End time must be after start time");
    }

    @Test
    void create_itemAlreadyHasActiveAuction_throwsIllegalArgument() {
        Item item = buildItem(1L);
        AuctionRequest request = buildRequest(1L);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        Auction existing = buildAuction(2L, item);
        existing.setStatus(AuctionStatus.ACTIVE);
        when(auctionRepository.findFirstByItemIdAndStatusNot(1L, AuctionStatus.CANCELLED))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> auctionService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already has an active or scheduled auction");
    }

    @Test
    void create_unknownItem_throwsItemNotFound() {
        AuctionRequest request = buildRequest(99L);
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> auctionService.create(request))
                .isInstanceOf(ItemNotFoundException.class);
    }

    @Test
    void updateStatus_setsAuctioningOnItem_whenStatusActive() {
        Item item = buildItem(1L);
        Auction auction = buildAuction(1L, item);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        auction.setStatus(AuctionStatus.ACTIVE);
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        when(auctionRepository.save(auction)).thenReturn(auction);

        AuctionResponse response = auctionService.updateStatus(1L, AuctionStatus.ACTIVE);
        assertThat(response.getStatus()).isEqualTo(AuctionStatus.ACTIVE);
        verify(itemRepository).save(item);
    }

    @Test
    void delete_existingScheduledAuction_deletesSuccessfully() {
        Item item = buildItem(1L);
        Auction auction = buildAuction(1L, item);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        auctionService.delete(1L);
        verify(auctionRepository).deleteById(1L);
    }

    @Test
    void delete_activeAuction_throwsIllegalArgument() {
        Item item = buildItem(1L);
        Auction auction = buildAuction(1L, item);
        auction.setStatus(AuctionStatus.ACTIVE);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));

        assertThatThrownBy(() -> auctionService.delete(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot delete an active auction");
    }

    @Test
    void delete_missingId_throwsNotFoundException() {
        when(auctionRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> auctionService.delete(99L))
                .isInstanceOf(AuctionNotFoundException.class);
    }
}

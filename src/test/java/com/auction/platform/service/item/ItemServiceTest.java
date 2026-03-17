package com.auction.platform.service.item;

import com.auction.platform.model.customer.Customer;
import com.auction.platform.service.customer.CustomerNotFoundException;
import com.auction.platform.repository.customer.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.auction.platform.controller.item.ItemMediaLinkRequest;
import com.auction.platform.controller.item.ItemRequest;
import com.auction.platform.controller.item.ItemResponse;
import com.auction.platform.model.item.Item;
import com.auction.platform.model.item.ItemCategory;
import com.auction.platform.model.item.ItemCondition;
import com.auction.platform.model.item.ItemMediaLink;
import com.auction.platform.model.item.ItemStatus;
import com.auction.platform.model.item.MediaType;
import com.auction.platform.repository.item.ItemMediaLinkRepository;
import com.auction.platform.repository.item.ItemRepository;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemMediaLinkRepository itemMediaLinkRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private ItemService itemService;

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

    private Item buildItem(Long id, Customer owner) {
        Item item = new Item();
        item.setId(id);
        item.setTitle("Vintage Camera");
        item.setDescription("A classic film camera");
        item.setCategory(ItemCategory.ELECTRONICS);
        item.setCondition(ItemCondition.GOOD);
        item.setStatus(ItemStatus.DRAFT);
        item.setOwner(owner);
        item.setMediaLinks(new ArrayList<>());
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        return item;
    }

    @Test
    void findAll_returnsAllItems() {
        Customer owner = buildCustomer(1L);
        when(itemRepository.findAll()).thenReturn(List.of(buildItem(1L, owner)));
        List<ItemResponse> result = itemService.findAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Vintage Camera");
    }

    @Test
    void findById_existingId_returnsItem() {
        Customer owner = buildCustomer(1L);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(buildItem(1L, owner)));
        ItemResponse response = itemService.findById(1L);
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Vintage Camera");
    }

    @Test
    void findById_missingId_throwsNotFoundException() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> itemService.findById(99L))
                .isInstanceOf(ItemNotFoundException.class);
    }

    @Test
    void findByOwnerId_unknownOwner_throwsCustomerNotFound() {
        when(customerRepository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> itemService.findByOwnerId(99L))
                .isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    void findByOwnerId_validOwner_returnsItems() {
        Customer owner = buildCustomer(1L);
        when(customerRepository.existsById(1L)).thenReturn(true);
        when(itemRepository.findByOwnerId(1L)).thenReturn(List.of(buildItem(1L, owner)));
        List<ItemResponse> result = itemService.findByOwnerId(1L);
        assertThat(result).hasSize(1);
    }

    @Test
    void create_validRequest_savesAndReturnsItem() {
        ItemRequest request = ItemRequest.builder()
                .title("Vintage Camera")
                .description("A classic film camera")
                .category(ItemCategory.ELECTRONICS)
                .condition(ItemCondition.GOOD)
                .ownerId(1L)
                .build();

        Customer owner = buildCustomer(1L);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(owner));

        Item saved = buildItem(1L, owner);
        when(itemRepository.save(any(Item.class))).thenReturn(saved);

        ItemResponse response = itemService.create(request);
        assertThat(response.getTitle()).isEqualTo("Vintage Camera");
        assertThat(response.getStatus()).isEqualTo(ItemStatus.DRAFT);
    }

    @Test
    void create_withMediaLinks_savesMediaLinks() {
        ItemMediaLinkRequest mediaRequest = new ItemMediaLinkRequest(
                "https://s3.example.com/image.jpg", MediaType.IMAGE, 0);
        ItemRequest request = ItemRequest.builder()
                .title("Vintage Camera")
                .category(ItemCategory.ELECTRONICS)
                .condition(ItemCondition.GOOD)
                .ownerId(1L)
                .mediaLinks(List.of(mediaRequest))
                .build();

        Customer owner = buildCustomer(1L);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(owner));

        Item saved = buildItem(1L, owner);
        ItemMediaLink link = new ItemMediaLink(1L, saved,
                "https://s3.example.com/image.jpg", MediaType.IMAGE, 0);
        saved.setMediaLinks(List.of(link));
        when(itemRepository.save(any(Item.class))).thenReturn(saved);

        ItemResponse response = itemService.create(request);
        assertThat(response.getMediaLinks()).hasSize(1);
        assertThat(response.getMediaLinks().get(0).getUrl())
                .isEqualTo("https://s3.example.com/image.jpg");
    }

    @Test
    void create_unknownOwner_throwsCustomerNotFound() {
        ItemRequest request = ItemRequest.builder()
                .title("Camera")
                .category(ItemCategory.ELECTRONICS)
                .condition(ItemCondition.NEW)
                .ownerId(99L)
                .build();
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> itemService.create(request))
                .isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    void updateStatus_existingItem_updatesStatus() {
        Customer owner = buildCustomer(1L);
        Item item = buildItem(1L, owner);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        item.setStatus(ItemStatus.READY);
        when(itemRepository.save(item)).thenReturn(item);

        ItemResponse response = itemService.updateStatus(1L, ItemStatus.READY);
        assertThat(response.getStatus()).isEqualTo(ItemStatus.READY);
    }

    @Test
    void delete_existingId_deletesItem() {
        when(itemRepository.existsById(1L)).thenReturn(true);
        itemService.delete(1L);
        verify(itemRepository).deleteById(1L);
    }

    @Test
    void delete_missingId_throwsNotFoundException() {
        when(itemRepository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> itemService.delete(99L))
                .isInstanceOf(ItemNotFoundException.class);
    }

    @Test
    void addMediaLink_existingItem_addsLink() {
        Customer owner = buildCustomer(1L);
        Item item = buildItem(1L, owner);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        ItemMediaLinkRequest request = new ItemMediaLinkRequest(
                "https://s3.example.com/video.mp4", MediaType.VIDEO, 1);

        when(itemRepository.save(item)).thenReturn(item);

        ItemResponse response = itemService.addMediaLink(1L, request);
        assertThat(response.getMediaLinks()).hasSize(1);
        assertThat(response.getMediaLinks().get(0).getMediaType()).isEqualTo(MediaType.VIDEO);
    }

    @Test
    void removeMediaLink_wrongItem_throwsIllegalArgument() {
        Customer owner = buildCustomer(1L);
        Item item1 = buildItem(1L, owner);
        Item item2 = buildItem(2L, owner);

        ItemMediaLink link = new ItemMediaLink(10L, item2,
                "https://s3.example.com/image.jpg", MediaType.IMAGE, 0);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(itemMediaLinkRepository.findById(10L)).thenReturn(Optional.of(link));

        assertThatThrownBy(() -> itemService.removeMediaLink(1L, 10L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

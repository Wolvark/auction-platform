package com.auction.platform.bid;

import com.auction.platform.customer.Customer;
import com.auction.platform.customer.CustomerNotFoundException;
import com.auction.platform.customer.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BidServiceTest {

    @Mock
    private BidRepository bidRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private BidService bidService;

    private Customer buildCustomer(Long id) {
        Customer c = new Customer();
        c.setId(id);
        c.setFirstName("John");
        c.setLastName("Doe");
        c.setEmail("john@example.com");
        c.setCreatedAt(LocalDateTime.now());
        c.setUpdatedAt(LocalDateTime.now());
        return c;
    }

    private Bid buildBid(Long id, Customer customer) {
        Bid b = new Bid();
        b.setId(id);
        b.setItemName("Vintage Watch");
        b.setAmount(new BigDecimal("150.00"));
        b.setCustomer(customer);
        b.setStatus(BidStatus.PENDING);
        b.setCreatedAt(LocalDateTime.now());
        b.setUpdatedAt(LocalDateTime.now());
        return b;
    }

    @Test
    void findAll_returnsAllBids() {
        Customer customer = buildCustomer(1L);
        when(bidRepository.findAll()).thenReturn(List.of(buildBid(1L, customer)));
        List<BidResponse> result = bidService.findAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getItemName()).isEqualTo("Vintage Watch");
    }

    @Test
    void findById_existingId_returnsBid() {
        Customer customer = buildCustomer(1L);
        Bid bid = buildBid(1L, customer);
        when(bidRepository.findById(1L)).thenReturn(Optional.of(bid));
        BidResponse response = bidService.findById(1L);
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void findById_missingId_throwsNotFoundException() {
        when(bidRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> bidService.findById(99L))
                .isInstanceOf(BidNotFoundException.class);
    }

    @Test
    void create_validRequest_savesAndReturnsBid() {
        BidRequest request = new BidRequest("Vintage Watch", new BigDecimal("150.00"), 1L);
        Customer customer = buildCustomer(1L);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        Bid saved = buildBid(1L, customer);
        when(bidRepository.save(any(Bid.class))).thenReturn(saved);

        BidResponse response = bidService.create(request);
        assertThat(response.getItemName()).isEqualTo("Vintage Watch");
        assertThat(response.getStatus()).isEqualTo(BidStatus.PENDING);
    }

    @Test
    void create_unknownCustomer_throwsCustomerNotFound() {
        BidRequest request = new BidRequest("Watch", new BigDecimal("10.00"), 99L);
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> bidService.create(request))
                .isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    void updateStatus_existingBid_updatesStatus() {
        Customer customer = buildCustomer(1L);
        Bid bid = buildBid(1L, customer);
        when(bidRepository.findById(1L)).thenReturn(Optional.of(bid));
        bid.setStatus(BidStatus.ACCEPTED);
        when(bidRepository.save(bid)).thenReturn(bid);

        BidResponse response = bidService.updateStatus(1L, BidStatus.ACCEPTED);
        assertThat(response.getStatus()).isEqualTo(BidStatus.ACCEPTED);
    }

    @Test
    void delete_existingId_deletesBid() {
        when(bidRepository.existsById(1L)).thenReturn(true);
        bidService.delete(1L);
        verify(bidRepository).deleteById(1L);
    }

    @Test
    void delete_missingId_throwsNotFoundException() {
        when(bidRepository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> bidService.delete(99L))
                .isInstanceOf(BidNotFoundException.class);
    }
}

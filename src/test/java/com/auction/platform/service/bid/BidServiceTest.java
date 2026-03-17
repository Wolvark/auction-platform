package com.auction.platform.service.bid;

import com.auction.platform.model.account.Account;
import com.auction.platform.model.customer.Customer;
import com.auction.platform.repository.account.AccountRepository;
import com.auction.platform.service.account.AccountNotFoundException;
import com.auction.platform.service.customer.CustomerNotFoundException;
import com.auction.platform.repository.customer.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import com.auction.platform.controller.bid.BidRequest;
import com.auction.platform.controller.bid.BidResponse;
import com.auction.platform.model.bid.Bid;
import com.auction.platform.model.bid.BidStatus;
import com.auction.platform.repository.bid.BidRepository;

@ExtendWith(MockitoExtension.class)
class BidServiceTest {

    @Mock
    private BidRepository bidRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

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

    private Account buildAccount(Customer customer, BigDecimal balance, BigDecimal heldAmount) {
        Account a = new Account();
        a.setId(1L);
        a.setCustomer(customer);
        a.setBalance(balance);
        a.setHeldAmount(heldAmount);
        return a;
    }

    private Bid buildBid(Long id, Customer customer, BidStatus status) {
        Bid b = new Bid();
        b.setId(id);
        b.setItemName("Vintage Watch");
        b.setAmount(new BigDecimal("150.00"));
        b.setCustomer(customer);
        b.setAuctionId(10L);
        b.setStatus(status);
        b.setCreatedAt(LocalDateTime.now());
        b.setUpdatedAt(LocalDateTime.now());
        return b;
    }

    @Test
    void findAll_returnsAllBids() {
        Customer customer = buildCustomer(1L);
        when(bidRepository.findAll()).thenReturn(List.of(buildBid(1L, customer, BidStatus.ACTIVE)));
        List<BidResponse> result = bidService.findAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getItemName()).isEqualTo("Vintage Watch");
    }

    @Test
    void findById_existingId_returnsBid() {
        Customer customer = buildCustomer(1L);
        Bid bid = buildBid(1L, customer, BidStatus.ACTIVE);
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
        BidRequest request = new BidRequest("Vintage Watch", new BigDecimal("150.00"), 1L, 10L);
        Customer customer = buildCustomer(1L);
        Account account = buildAccount(customer, new BigDecimal("500.00"), BigDecimal.ZERO);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(accountRepository.findByCustomerIdForUpdate(1L)).thenReturn(Optional.of(account));
        when(bidRepository.findByAuctionIdAndStatus(10L, BidStatus.ACTIVE)).thenReturn(List.of());
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Bid saved = buildBid(1L, customer, BidStatus.ACTIVE);
        when(bidRepository.save(any(Bid.class))).thenReturn(saved);

        BidResponse response = bidService.create(request);
        assertThat(response.getItemName()).isEqualTo("Vintage Watch");
        assertThat(response.getStatus()).isEqualTo(BidStatus.ACTIVE);
        assertThat(response.getAuctionId()).isEqualTo(10L);
        assertThat(account.getHeldAmount()).isEqualByComparingTo(new BigDecimal("150.00"));
        verify(messagingTemplate).convertAndSend(eq("/topic/auction/10/bids"), any(BidResponse.class));
    }

    @Test
    void create_insufficientBalance_throwsInsufficientBalanceException() {
        BidRequest request = new BidRequest("Vintage Watch", new BigDecimal("600.00"), 1L, 10L);
        Customer customer = buildCustomer(1L);
        Account account = buildAccount(customer, new BigDecimal("500.00"), BigDecimal.ZERO);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(accountRepository.findByCustomerIdForUpdate(1L)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> bidService.create(request))
                .isInstanceOf(InsufficientBalanceException.class);
        verify(bidRepository, never()).save(any());
    }

    @Test
    void create_outbidsPreviousActiveBidder() {
        BidRequest request = new BidRequest("Vintage Watch", new BigDecimal("200.00"), 1L, 10L);
        Customer newBidder = buildCustomer(1L);
        Customer previousBidder = buildCustomer(2L);

        Account newAccount = buildAccount(newBidder, new BigDecimal("500.00"), BigDecimal.ZERO);
        Account previousAccount = buildAccount(previousBidder, new BigDecimal("300.00"), new BigDecimal("150.00"));

        Bid previousBid = buildBid(99L, previousBidder, BidStatus.ACTIVE);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(newBidder));
        when(accountRepository.findByCustomerIdForUpdate(1L)).thenReturn(Optional.of(newAccount));
        when(bidRepository.findByAuctionIdAndStatus(10L, BidStatus.ACTIVE)).thenReturn(List.of(previousBid));
        when(accountRepository.findByCustomerIdForUpdate(2L)).thenReturn(Optional.of(previousAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bidRepository.save(any(Bid.class))).thenAnswer(inv -> inv.getArgument(0));

        bidService.create(request);

        assertThat(previousBid.getStatus()).isEqualTo(BidStatus.OUTBID);
        assertThat(previousAccount.getHeldAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(newAccount.getHeldAmount()).isEqualByComparingTo(new BigDecimal("200.00"));
    }

    @Test
    void create_outbidPreviousBidderAccountNotFound_throwsAccountNotFoundException() {
        BidRequest request = new BidRequest("Vintage Watch", new BigDecimal("200.00"), 1L, 10L);
        Customer newBidder = buildCustomer(1L);
        Customer previousBidder = buildCustomer(2L);

        Account newAccount = buildAccount(newBidder, new BigDecimal("500.00"), BigDecimal.ZERO);
        Bid previousBid = buildBid(99L, previousBidder, BidStatus.ACTIVE);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(newBidder));
        when(accountRepository.findByCustomerIdForUpdate(1L)).thenReturn(Optional.of(newAccount));
        when(bidRepository.findByAuctionIdAndStatus(10L, BidStatus.ACTIVE)).thenReturn(List.of(previousBid));
        when(accountRepository.findByCustomerIdForUpdate(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bidService.create(request))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void create_noAccount_throwsAccountNotFoundException() {
        BidRequest request = new BidRequest("Watch", new BigDecimal("10.00"), 1L, 10L);
        Customer customer = buildCustomer(1L);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(accountRepository.findByCustomerIdForUpdate(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bidService.create(request))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void create_unknownCustomer_throwsCustomerNotFound() {
        BidRequest request = new BidRequest("Watch", new BigDecimal("10.00"), 99L, 10L);
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> bidService.create(request))
                .isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    void updateStatus_existingBid_updatesStatus() {
        Customer customer = buildCustomer(1L);
        Bid bid = buildBid(1L, customer, BidStatus.ACTIVE);
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

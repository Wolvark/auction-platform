package com.auction.platform.customer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer buildCustomer(Long id, String email) {
        Customer c = new Customer();
        c.setId(id);
        c.setFirstName("John");
        c.setLastName("Doe");
        c.setEmail(email);
        c.setPhone("123");
        c.setCreatedAt(LocalDateTime.now());
        c.setUpdatedAt(LocalDateTime.now());
        return c;
    }

    @Test
    void findAll_returnsAllCustomers() {
        when(customerRepository.findAll()).thenReturn(List.of(buildCustomer(1L, "john@example.com")));
        List<CustomerResponse> result = customerService.findAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void findById_existingId_returnsCustomer() {
        Customer customer = buildCustomer(1L, "john@example.com");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        CustomerResponse response = customerService.findById(1L);
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void findById_missingId_throwsNotFoundException() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> customerService.findById(99L))
                .isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    void create_newEmail_savesAndReturnsCustomer() {
        CustomerRequest request = new CustomerRequest("Jane", "Doe", "jane@example.com", null);
        when(customerRepository.existsByEmail("jane@example.com")).thenReturn(false);
        Customer saved = buildCustomer(2L, "jane@example.com");
        when(customerRepository.save(any(Customer.class))).thenReturn(saved);

        CustomerResponse response = customerService.create(request);
        assertThat(response.getEmail()).isEqualTo("jane@example.com");
    }

    @Test
    void create_duplicateEmail_throwsIllegalArgument() {
        CustomerRequest request = new CustomerRequest("Jane", "Doe", "jane@example.com", null);
        when(customerRepository.existsByEmail("jane@example.com")).thenReturn(true);
        assertThatThrownBy(() -> customerService.create(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void delete_existingId_deletesCustomer() {
        when(customerRepository.existsById(1L)).thenReturn(true);
        customerService.delete(1L);
        verify(customerRepository).deleteById(1L);
    }

    @Test
    void delete_missingId_throwsNotFoundException() {
        when(customerRepository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> customerService.delete(99L))
                .isInstanceOf(CustomerNotFoundException.class);
    }
}

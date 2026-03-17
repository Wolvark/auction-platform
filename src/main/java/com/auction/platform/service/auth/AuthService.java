package com.auction.platform.service.auth;

import com.auction.platform.controller.auth.AuthResponse;
import com.auction.platform.controller.auth.LoginRequest;
import com.auction.platform.controller.auth.RegisterRequest;
import com.auction.platform.model.account.Account;
import com.auction.platform.model.customer.Customer;
import com.auction.platform.model.customer.CustomerRole;
import com.auction.platform.repository.account.AccountRepository;
import com.auction.platform.repository.customer.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email '" + request.getEmail() + "' is already in use");
        }
        if (customerRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username '" + request.getUsername() + "' is already taken");
        }

        Customer customer = Customer.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(CustomerRole.CUSTOMER)
                .build();

        customer = customerRepository.save(customer);

        // Create account for new customer
        Account account = Account.builder()
                .customer(customer)
                .build();
        accountRepository.save(account);

        String token = jwtService.generateToken(customer);
        return AuthResponse.builder()
                .token(token)
                .customerId(customer.getId())
                .username(customer.getUsername())
                .email(customer.getEmail())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        Customer customer = customerRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new org.springframework.security.authentication.BadCredentialsException("Invalid username or password"));

        String token = jwtService.generateToken(customer);
        return AuthResponse.builder()
                .token(token)
                .customerId(customer.getId())
                .username(customer.getUsername())
                .email(customer.getEmail())
                .build();
    }
}

package com.auction.platform.controller.customer;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String username;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

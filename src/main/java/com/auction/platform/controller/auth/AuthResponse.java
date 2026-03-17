package com.auction.platform.controller.auth;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private Long customerId;
    private String username;
    private String email;
}

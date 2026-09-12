package com.pm.authservice.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
@Getter
@RequiredArgsConstructor
public class LoginResponseDTO {
    private final String token;
}

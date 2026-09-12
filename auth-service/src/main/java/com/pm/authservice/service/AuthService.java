package com.pm.authservice.service;

import com.pm.authservice.dto.LoginRequestDTO;
import com.pm.authservice.exception.InvalidCredentialException;
import com.pm.authservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public String authenticate(LoginRequestDTO loginRequestDTO) {

        return userService
                .findByEmail(loginRequestDTO.getEmail())
                .map(user -> {

                    boolean passwordMatches = passwordEncoder.matches(
                            loginRequestDTO.getPassword(),
                            user.getPassword()
                    );

                    if (!passwordMatches) {
                        throw new InvalidCredentialException("Invalid password");
                    }

                    return jwtUtil.generateToken(
                            user.getEmail(),
                            user.getRole()
                    );
                })
                .orElseThrow(() ->
                        new InvalidCredentialException("User not found")
                );
    }
}
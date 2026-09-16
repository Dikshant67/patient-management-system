package com.pm.authservice.service;

import com.pm.authservice.dto.LoginRequestDTO;
import com.pm.authservice.exception.InvalidCredentialException;
import com.pm.authservice.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
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
    public boolean validateToken(String token) {
        try {
            log.info("Validating JWT Token : validateToken() AuthService");
            jwtUtil.validateToken(token);
            return true;
        }catch (JwtException e){
        return  false;
        }
    }
}
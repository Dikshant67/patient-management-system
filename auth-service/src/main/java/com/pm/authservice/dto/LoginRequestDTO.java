package com.pm.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequestDTO {
    @NotBlank(message = "email is required")
    @Email(message = "Email should be valid email address")
    private String email;

    @NotBlank(message ="password is required" )
    @Size(min = 8 , message = "Password must be at least 8 chars long",max = 13)
    private String password;

}

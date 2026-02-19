package com.resumebuilder.resumebuilderapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

/*
This class is the Data Transfer Object of the login request
Two fields of the user will be coming from the request i.e. email and password
Required Spring validation is used on both the fields
 */
public class LoginRequest {

    @NotBlank(message = "email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;




}
package com.resumebuilder.resumebuilderapi.controller;

import com.resumebuilder.resumebuilderapi.dto.AuthResponse;
import com.resumebuilder.resumebuilderapi.dto.LoginRequest;
import com.resumebuilder.resumebuilderapi.dto.RegisterRequest;
import com.resumebuilder.resumebuilderapi.service.AuthService;
import com.resumebuilder.resumebuilderapi.service.FileUploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static com.resumebuilder.resumebuilderapi.util.AppConstants.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(AUTH_CONTROLLER)
public class AuthController {

    private final AuthService authService;

    private final FileUploadService fileUploadService;

    @PostMapping(REGISTER)
    public ResponseEntity<?> register (@Valid @RequestBody RegisterRequest request) {
        log.info("Inside AuthController - register(): {}", request);
        AuthResponse response = authService.register(request);
        log.info("Response from service: {}", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @GetMapping(VERIFY_EMAIL)
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        log.info("Inside AuthController - verifyEmail(): {}", token);
        authService.verifyEmail(token);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "email verified successfully"));
    }

    @PostMapping(UPLOAD_IMAGE)
    public ResponseEntity<?> uploadImage(@RequestPart("image")MultipartFile file) throws IOException {
        Map<String, String> response = fileUploadService.uploadSingleImage(file);
        return ResponseEntity.ok(response);
    }

    @PostMapping(LOGIN)
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(RESEND_VERIFICATION)
    public ResponseEntity<?> resendVerification(@RequestBody Map<String, String> body) {
        // Step1: Get the email from request
        String email = body.get("email");
        // Step2: Add the validations
        if (Objects.isNull(email)) {
            return ResponseEntity.badRequest().body(Map.of("message", "email is required"));
        }
        // Step3: Call the service method to resend the verification link
        authService.resendVerification(email);
        // Step4: Return response
        return ResponseEntity.ok(Map.of("success", true, "message", "verification email send"));

    }

}
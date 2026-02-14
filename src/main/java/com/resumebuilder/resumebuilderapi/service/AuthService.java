package com.resumebuilder.resumebuilderapi.service;

import com.resumebuilder.resumebuilderapi.document.User;
import com.resumebuilder.resumebuilderapi.dto.AuthResponse;
import com.resumebuilder.resumebuilderapi.dto.RegisterRequest;
import com.resumebuilder.resumebuilderapi.exception.ResourceExistsException;
import com.resumebuilder.resumebuilderapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService  {

    @Autowired
    private final UserRepository userRepository;

    @Value("${app.base.url:http://localhost:8080/}" )
    private String appBaseUrl;

    private final EmailService emailService;

    private AuthResponse toResponse(User newUser) {
        return AuthResponse.builder()
                .id(newUser.getId())
                .name(newUser.getName())
                .email(newUser.getName())
                .profileImageUrl(newUser.getProfileImageUrl())
                .emailVerified(newUser.getEmailVerified())
                .subscriptionPlan(newUser.getSubscriptionPlan())
                .createdAt(newUser.getCreatedAt())
                .updatedAt(newUser.getUpdatedAt())
                .build();
    }

    private User toDocument (RegisterRequest request) {
        return User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .profileImageUrl(request.getProfileImageUrl())
                .subscriptionPlan("Basic")
                .emailVerified(false)
                .verificationToken(UUID.randomUUID().toString())
                .verificationExpires(LocalDateTime.now().plusHours(24))
                .build();
    }

    private void sendVerificationEmail(User newUser) {
        log.info("Inside AuthService - sendVerificationEmail(): {}", newUser);
         try {
            String link = appBaseUrl + "api/auth/verify-email?token="+newUser.getVerificationToken();
             String html = "<div style='font-family:sans-serif;'>" +
                     "<h2>Verify your email</h2>" +
                     "<p>Hi " + newUser.getName() + ", please confirm your email to activate your account.</p>" +
                     "<p>" +
                     "<a href='" + link + "' " +
                     "style='display:inline-block; padding:10px 16px; background:#6366f1; color:#fff; border-radius:6px; text-decoration:none;'>" +
                     "Verify Email</a>" +
                     "</p>" +
                     "<p>Or copy this link: " + link + "</p>" +
                     "<p>This link expires in 24 hours.</p>" +
                     "</div>";


             emailService.sendHtmlEmail(newUser.getEmail(), "Verify your email", html);

         } catch (Exception e) {
             log.error("Exception occured at sendVerification(): {}", e.getMessage());
             throw new RuntimeException("Failed to send verification email: " + e.getMessage());
         }
    }

    public AuthResponse register (RegisterRequest request) {
        log.info("Inside AuthService: register() {}", request);

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceExistsException("User already exists with this email");
        }

        User newUser = toDocument(request);

        userRepository.save(newUser);

        // TODO: Send Verification email
        sendVerificationEmail(newUser);
        return toResponse(newUser);


    }

    public void verifyEmail (String token) {
        log.info("Inside AuthService: verifyEmail(): {}", token);
        User user = userRepository.findByVerificationToken(token).orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        if (user.getVerificationExpires() != null && user.getVerificationExpires().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification token is expired. Please request new one.");
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationExpires(null);

        userRepository.save(user);

    }




}
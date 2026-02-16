package com.resumebuilder.resumebuilderapi.service;

import com.resumebuilder.resumebuilderapi.document.User;
import com.resumebuilder.resumebuilderapi.dto.AuthResponse;
import com.resumebuilder.resumebuilderapi.dto.LoginRequest;
import com.resumebuilder.resumebuilderapi.dto.RegisterRequest;
import com.resumebuilder.resumebuilderapi.exception.ResourceExistsException;
import com.resumebuilder.resumebuilderapi.repository.UserRepository;
import com.resumebuilder.resumebuilderapi.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService  {

    // TODO: Base URL from application.properties file to form the complete verification link
    @Value("${app.base.url:http://localhost:8080/}" )
    private String appBaseUrl;

    // TODO: Dependencies injection
    @Autowired
    private final UserRepository userRepository;

    private final EmailService emailService;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

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
                .password(passwordEncoder.encode(request.getPassword()))
                .profileImageUrl(request.getProfileImageUrl())
                .subscriptionPlan("Basic")
                .emailVerified(false)
                .verificationToken(UUID.randomUUID().toString())
                .verificationExpires(LocalDateTime.now().plusHours(24))
                .build();
    }

    // TODO: Method to send verification email
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

    // TODO: Method to register new user
    public AuthResponse register (RegisterRequest request) {
        log.info("Inside AuthService: register() {}", request);

        // TODO: If user already exists with same email id
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceExistsException("User already exists with this email");
        }

        // TODO: If user is not not already registered, save the request into a new user
        User newUser = toDocument(request);

        // TODO: Save the user
        userRepository.save(newUser);

        // TODO: Send the verification email
        sendVerificationEmail(newUser);

        // TODO: Return the response
        return toResponse(newUser);


    }


    /*
    This method is used to verify the email and it accepts a json web token
    The token will be verified by the backend
    In case verification token is expired, it will throw an exception that verification token is expired
    If the token is successfully verified, the emailVerified field will be set to true
    Finally user will be saved in db
     */
    // TODO: Method to verify email
    public void verifyEmail (String token) {
        log.info("Inside AuthService: verifyEmail(): {}", token);

        // TODO: Check if the user is registered by email or not
        User user = userRepository.findByVerificationToken(token).orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        // TODO: Exception if token expired
        if (user.getVerificationExpires() != null && user.getVerificationExpires().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification token is expired. Please request new one.");
        }

        // TODO: Set the emailVerified field to true
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationExpires(null);

        // TODO: Save the user
        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        User existingUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new UsernameNotFoundException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), existingUser.getPassword())) {
            throw  new UsernameNotFoundException("Invalid email or password");
        }

        if(!existingUser.getEmailVerified()) {
            throw new RuntimeException("Please verify your email before loggin in");
        }

        // TODO: JWT Token is not hard coded. Later it will be added
        String token = jwtUtil.genrateToken(existingUser.getId());
        AuthResponse response = toResponse(existingUser);
        response.setToken(token);
        return response;


    }


    public void resendVerification(String email) {
        // TODO: Fetch the user account by email
        User user = userRepository.findByEmail(email).orElseThrow(()-> new RuntimeException("User not found"));
        // TODO: Check the email is verified or not
        if (user.getEmailVerified()) {
            throw new RuntimeException("User is already verified");
        }
        // TODO: Set the new verification token and expires time
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setVerificationExpires(LocalDateTime.now().plusHours(24));

        // TODO: Update the user
        userRepository.save(user);
        // TODO: Resend the verification email
        sendVerificationEmail(user);

    }
}
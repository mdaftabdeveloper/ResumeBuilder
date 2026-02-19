package com.resumebuilder.resumebuilderapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AuthResponse is a Data Transfer Object (DTO) used to send authentication-related
 * information back to the client after successful login or registration.
 *
 * <p>This class contains user details along with the generated JWT token.
 * It is typically returned as a JSON response in authentication APIs.</p>
 *
 * <p>Key Information Included:</p>
 * <ul>
 *     <li>User basic details (name, email)</li>
 *     <li>Profile image URL</li>
 *     <li>Subscription plan information</li>
 *     <li>Email verification status</li>
 *     <li>JWT authentication token</li>
 *     <li>Account creation and update timestamps</li>
 * </ul>
 *
 * <p>The field {@code id} is mapped as {@code _id} in JSON output using
 * {@link JsonProperty} annotation, which is useful when working with MongoDB
 * where the primary key is usually stored as {@code _id}.</p>
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    /**
     * The unique identifier of the user.
     *
     * <p>This field is serialized as {@code _id} in the JSON response.</p>
     */
    @JsonProperty("_id")
    private String id;

    /**
     * The full name of the authenticated user.
     */
    private String name;

    /**
     * The email address of the authenticated user.
     */
    private String email;

    /**
     * The profile image URL of the user (stored on Cloudinary or any external storage).
     */
    private String profileImageUrl;

    /**
     * The subscription plan of the user (example: FREE, BASIC, PREMIUM).
     */
    private String subscriptionPlan;

    /**
     * Indicates whether the user's email is verified or not.
     *
     * <p>true = verified, false = not verified</p>
     */
    private Boolean emailVerified;

    /**
     * The JWT token generated for authentication.
     *
     * <p>This token is required to access secured APIs.</p>
     */
    private String token;

    /**
     * The date and time when the user account was created.
     */
    private LocalDateTime createdAt;

    /**
     * The date and time when the user account was last updated.
     */
    private LocalDateTime updatedAt;

}
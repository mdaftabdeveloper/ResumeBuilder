package com.resumebuilder.resumebuilderapi.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * JwtUtil is a utility class responsible for generating and validating JWT tokens.
 *
 * <p>This class is used in the authentication system of the ResumeBuilder API.
 * It provides methods to:</p>
 *
 * <ul>
 *     <li>Generate a JWT token for a user</li>
 *     <li>Extract the userId (subject) from a token</li>
 *     <li>Validate a token signature and format</li>
 *     <li>Check whether a token is expired</li>
 * </ul>
 *
 * <p>The JWT secret key and token expiration time are injected from
 * the application configuration file (application.properties or application.yml).</p>
 *
 * <p>Example configuration:</p>
 * <pre>
 * jwt.secret=yourSecretKey
 * jwt.expiration=86400000
 * </pre>
 *
 * <p>This class uses the JJWT library to handle token creation and parsing.</p>
 */
@Component
public class JwtUtil {

    /**
     * Secret key used to sign and verify JWT tokens.
     *
     * <p>This value is loaded from application properties using the key {@code jwt.secret}.</p>
     */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * Token expiration time in milliseconds.
     *
     * <p>This value is loaded from application properties using the key {@code jwt.expiration}.</p>
     */
    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    /**
     * Generates and returns the signing key used to sign JWT tokens.
     *
     * <p>This method converts the configured secret string into a secure HMAC SHA key.</p>
     *
     * @return the signing key used for JWT signature
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Generates a JWT token for the given userId.
     *
     * <p>The generated token contains:</p>
     * <ul>
     *     <li>Subject: userId</li>
     *     <li>Issued time: current date/time</li>
     *     <li>Expiration time: based on configured expiration value</li>
     * </ul>
     *
     * <p>The token is signed using the secret signing key.</p>
     *
     * @param userId the unique user ID that will be stored in the token subject
     * @return a signed JWT token as a String
     */
    public String genrateToken(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey())
                .compact();

    }

    /**
     * Extracts the userId (subject) from the provided JWT token.
     *
     * <p>This method parses the token using the signing key and retrieves
     * the subject field which stores the userId.</p>
     *
     * @param token the JWT token from which the userId should be extracted
     * @return the userId stored inside the token
     * @throws JwtException if the token is invalid or cannot be parsed
     */
    public String generateUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(getSigningKey())
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    /**
     * Validates the JWT token by checking its signature and format.
     *
     * <p>This method does not directly check expiration, it only verifies whether
     * the token can be parsed successfully using the signing key.</p>
     *
     * @param token the JWT token to validate
     * @return true if the token is valid, otherwise false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Checks whether the JWT token is expired or not.
     *
     * <p>This method extracts the expiration date from the token and compares it
     * with the current system time.</p>
     *
     * <p>If the token is invalid or cannot be parsed, the method assumes the token
     * is expired and returns true.</p>
     *
     * @param token the JWT token to check
     * @return true if the token is expired or invalid, otherwise false
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return true;
        }
    }
}
package com.resumebuilder.resumebuilderapi.config;

import com.resumebuilder.resumebuilderapi.security.JwtAuthenticationEntryPoint;
import com.resumebuilder.resumebuilderapi.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * SecurityConfig is the main Spring Security configuration class for the ResumeBuilder API.
 *
 * <p>This class is responsible for configuring:</p>
 * <ul>
 *     <li>JWT-based authentication and authorization</li>
 *     <li>Security filter chain setup</li>
 *     <li>Stateless session management</li>
 *     <li>Public and protected API endpoints</li>
 *     <li>CORS configuration for frontend communication</li>
 *     <li>Password encryption using BCrypt</li>
 * </ul>
 *
 * <p>It ensures that only authenticated users can access protected endpoints,
 * while allowing public access to authentication-related endpoints such as login,
 * registration, email verification, and image upload.</p>
 *
 * <p>This configuration uses JWT authentication, so sessions are disabled
 * (STATELESS) and authentication is performed using tokens.</p>
 */

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * JWT authentication filter responsible for validating JWT tokens
     * in incoming HTTP requests.
     */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Creates and provides a PasswordEncoder bean using BCrypt hashing algorithm.
     *
     * <p>BCrypt is a strong password hashing function that helps secure user passwords
     * by encrypting them before saving into the database.</p>
     *
     * @return a BCrypt-based PasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the main security filter chain for the application.
     *
     * <p>This method defines how Spring Security should handle incoming requests.</p>
     *
     * <p>Key security configurations:</p>
     * <ul>
     *     <li>Enables CORS using a custom CorsConfigurationSource</li>
     *     <li>Disables CSRF protection because the application uses JWT (stateless authentication)</li>
     *     <li>Allows public access to authentication endpoints</li>
     *     <li>Requires authentication for all other endpoints</li>
     *     <li>Configures stateless session management</li>
     *     <li>Adds JwtAuthenticationFilter before UsernamePasswordAuthenticationFilter</li>
     *     <li>Handles unauthorized requests using JwtAuthenticationEntryPoint</li>
     * </ul>
     *
     * @param http the HttpSecurity object used to configure web-based security
     * @return the configured SecurityFilterChain
     * @throws Exception if any error occurs during security configuration
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/verify-email",
                                "/api/auth/upload-image",
                                "/api/auth/resend-verification",
                                "/actuator/**"
                        )
                        .permitAll()
                        .anyRequest()
                        .authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                );

        return http.build();
    }

    /**
     * Defines and provides a CORS configuration for the application.
     *
     * <p>This configuration allows requests from the frontend application
     * running on {@code http://localhost:5173} (typically a React/Vite frontend).</p>
     *
     * <p>Allowed HTTP methods:</p>
     * <ul>
     *     <li>GET</li>
     *     <li>POST</li>
     *     <li>PUT</li>
     *     <li>PATCH</li>
     *     <li>DELETE</li>
     *     <li>OPTIONS</li>
     * </ul>
     *
     * <p>It also allows all headers and supports sending credentials
     * (such as Authorization tokens).</p>
     *
     * @return CorsConfigurationSource containing the CORS rules for the API
     */

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }


}
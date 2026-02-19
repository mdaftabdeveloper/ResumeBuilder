package com.resumebuilder.resumebuilderapi.security;

import com.resumebuilder.resumebuilderapi.document.User;
import com.resumebuilder.resumebuilderapi.repository.UserRepository;
import com.resumebuilder.resumebuilderapi.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * JwtAuthenticationFilter is a custom Spring Security filter that validates JWT tokens
 * for every incoming HTTP request.
 *
 * <p>This filter runs once per request because it extends {@link OncePerRequestFilter}.</p>
 *
 * <p>It checks the Authorization header for a JWT token in the following format:</p>
 * <pre>
 * Authorization: Bearer &lt;jwt_token&gt;
 * </pre>
 *
 * <p>If the token is valid and not expired, the filter extracts the userId from the token,
 * fetches the user details from the database, and sets the authenticated user inside
 * the {@link SecurityContextHolder}.</p>
 *
 * <p>This allows Spring Security to treat the request as authenticated and permit access
 * to secured API endpoints.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Utility class used for JWT operations such as extracting userId,
     * validating token signature, and checking expiration.
     */
    private final JwtUtil jwtUtil;

    /**
     * Repository used to fetch user details from the database based on userId.
     */
    private final UserRepository userRepository;

    /**
     * Filters every request to validate the JWT token and set authentication context.
     *
     * <p>This method performs the following steps:</p>
     * <ul>
     *     <li>Reads the Authorization header from the request</li>
     *     <li>Checks if the header starts with "Bearer"</li>
     *     <li>Extracts the token and reads userId from it</li>
     *     <li>Validates the token signature and checks if token is expired</li>
     *     <li>Fetches the user from the database using userId</li>
     *     <li>Creates an Authentication object and stores it in SecurityContext</li>
     *     <li>Passes the request to the next filter in the chain</li>
     * </ul>
     *
     * <p>If the token is invalid or user is not found, the request continues without authentication,
     * and Spring Security will block access to secured endpoints.</p>
     *
     * @param request the incoming HTTP request
     * @param response the outgoing HTTP response
     * @param filterChain the filter chain that passes the request to the next filter
     * @throws ServletException if a servlet-related error occurs
     * @throws IOException if an input/output error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        String token = null;
        String userId = null;

        if (authHeader != null && authHeader.startsWith("Bearer")) {
            token = authHeader.substring(7);
            try {
                userId = jwtUtil.generateUserIdFromToken(token);
            } catch (Exception e) {
                log.info("Token is not valid or available");
            }
        }

        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                if (jwtUtil.validateToken(token) && !jwtUtil.isTokenExpired(token)) {

                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());

                    authToken.setDetails(new WebAuthenticationDetailsSource()
                            .buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }

            } catch (Exception e) {
                log.info("Exception occurred while validating the token");
            }
        }

        filterChain.doFilter(request, response);
    }

}
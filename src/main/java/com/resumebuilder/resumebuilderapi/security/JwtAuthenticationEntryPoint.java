package com.resumebuilder.resumebuilderapi.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * JwtAuthenticationEntryPoint is a Spring Security component that handles
 * unauthorized access attempts in the application.
 *
 * <p>This class is triggered when a user tries to access a secured API endpoint
 * without providing a valid JWT token.</p>
 *
 * <p>Instead of returning a default HTML error response, this class returns a
 * JSON response with HTTP status 401 (Unauthorized).</p>
 *
 * <p>Example Response:</p>
 * <pre>
 * {
 *   "message": "Not authorized, no token"
 * }
 * </pre>
 */


@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    /**
     * This method is called automatically by Spring Security when an unauthorized
     * request is detected.
     *
     * <p>This happens when the request does not contain a JWT token or contains
     * an invalid/expired token.</p>
     *
     * <p>The method sends a JSON response with status code 401 (Unauthorized).</p>
     *
     * @param request        the incoming HTTP request
     * @param response       the HTTP response used to send the error back to the client
     * @param authException  the exception thrown due to authentication failure
     * @throws IOException       if an input/output error occurs while writing the response
     * @throws ServletException  if a servlet-related error occurs
     */

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", "Not authorized, no token");
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
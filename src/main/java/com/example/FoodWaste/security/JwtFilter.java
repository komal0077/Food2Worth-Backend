package com.example.FoodWaste.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

    private final JwtUtil jwtUtil;

    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Skip auth APIs
        String path = request.getServletPath();

        if (path.startsWith("/api/auth")) {

            filterChain.doFilter(request, response);

            return;
        }

        String authHeader = request.getHeader("Authorization");

        String token = null;

        String email = null;

        if (authHeader != null &&
                authHeader.startsWith("Bearer ")) {

            token = authHeader.substring(7);

            try {
                email = jwtUtil.extractEmail(token);
            } catch (JwtException e) {
                // Expired/malformed/tampered token — treat as unauthenticated
                // rather than letting the parser exception bubble up as a 500.
                log.debug("Rejected invalid JWT: {}", e.getMessage());
            }
        }

        if (email != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            try {
                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(email);

                if (jwtUtil.validateToken(token, userDetails.getUsername())) {

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authToken);
                }
            } catch (UsernameNotFoundException e) {
                log.debug("JWT subject does not match any user: {}", email);
            }
        }

        filterChain.doFilter(request, response);
    }
}

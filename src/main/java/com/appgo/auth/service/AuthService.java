package com.appgo.auth.service;

import com.appgo.auth.dto.LoginRequest;
import com.appgo.auth.dto.TokenResponse;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

/**
 * Orchestration du login et du refresh.
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService,
            JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    public TokenResponse login(LoginRequest request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        return jwtService.generateTokenResponse(authentication.getName(), authentication.getAuthorities());
    }

    public TokenResponse refresh(String refreshToken) {
        try {
            String username = jwtService.verifyRefreshToken(refreshToken).getSubject();
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            return jwtService.generateTokenResponse(userDetails.getUsername(), userDetails.getAuthorities());
        } catch (JWTVerificationException ex) {
            throw new BadCredentialsException("Invalid refresh token", ex);
        }
    }
}

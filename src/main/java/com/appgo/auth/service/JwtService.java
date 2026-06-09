package com.appgo.auth.service;

import com.appgo.auth.config.AuthProperties;
import com.appgo.auth.dto.TokenResponse;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Service de génération et validation des JWT.
 */
@Service
public class JwtService {

    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ACCESS_TOKEN = "access";
    private static final String REFRESH_TOKEN = "refresh";

    private final AuthProperties properties;
    private final Algorithm algorithm;

    public JwtService(AuthProperties properties) {
        this.properties = properties;
        this.algorithm = Algorithm.HMAC256(properties.getJwt().getSecret());
    }

    public TokenResponse generateTokenResponse(String subject, Collection<? extends GrantedAuthority> authorities) {
        String accessToken = generateToken(subject, ACCESS_TOKEN, properties.getJwt().getAccessTokenTtl(), authorities);
        String refreshToken = generateToken(subject, REFRESH_TOKEN, properties.getJwt().getRefreshTokenTtl(), authorities);
        return new TokenResponse(
                accessToken,
                refreshToken,
                "Bearer",
                properties.getJwt().getAccessTokenTtl().getSeconds(),
                properties.getJwt().getRefreshTokenTtl().getSeconds());
    }

    public DecodedJWT verifyAccessToken(String token) {
        return verifierFor(ACCESS_TOKEN).verify(token);
    }

    public DecodedJWT verifyRefreshToken(String token) {
        return verifierFor(REFRESH_TOKEN).verify(token);
    }

    private String generateToken(String subject, String tokenType, java.time.Duration ttl,
            Collection<? extends GrantedAuthority> authorities) {
        Instant now = Instant.now();
        String[] roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .toArray(String[]::new);

        return JWT.create()
                .withIssuer(properties.getJwt().getIssuer())
                .withSubject(subject)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plus(ttl)))
                .withClaim(TOKEN_TYPE_CLAIM, tokenType)
                .withArrayClaim("roles", roles)
                .sign(algorithm);
    }

    private JWTVerifier verifierFor(String expectedTokenType) {
        return JWT.require(algorithm)
                .withIssuer(properties.getJwt().getIssuer())
                .withClaim(TOKEN_TYPE_CLAIM, expectedTokenType)
                .build();
    }
}

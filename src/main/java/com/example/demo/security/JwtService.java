
package com.example.demo.security;
import com.example.demo.entity.User;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
/////////////////
    public JwtService() {
        String secret =
                "demo-secret-key-for-jwt-learning-2026-very-long";

        SecretKey secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
//NimbusJwtEncoder
        this.jwtEncoder =
                new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
    }

    public String generateToken(User user) {

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()

                .subject(user.getUsername())

                .issuedAt(now)

                .expiresAt(now.plus(15, ChronoUnit.MINUTES))

                .claim("userId", user.getId())

                .claim("username", user.getUsername())

                .claim("email", user.getEmail())

                .claim("firstName", user.getFirstName())

                .claim("lastName", user.getLastName())

                .claim("roles",
                        java.util.List.of(user.getRole().name()))

                .claim("companyId",
                        user.getCompany().getId())

                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(
                        org.springframework.security.oauth2.jwt
                                .JwsHeader.with(MacAlgorithm.HS256)
                                .build(),
                        claims
                )
        ).getTokenValue();
    }
}

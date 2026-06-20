package com.test.backend.iam.application.internal.outboundservices.tokens;

public interface TokenService {
    String generateToken(String email);
    String getEmailFromToken(String token);
    boolean validateToken(String token);
}

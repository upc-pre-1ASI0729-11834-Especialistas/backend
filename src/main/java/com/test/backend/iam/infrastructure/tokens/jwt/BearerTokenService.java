package com.test.backend.iam.infrastructure.tokens.jwt;

import com.test.backend.iam.application.internal.outboundservices.tokens.TokenService;
import jakarta.servlet.http.HttpServletRequest;

public interface BearerTokenService extends TokenService {
    String getBearerTokenFrom(HttpServletRequest request);
}

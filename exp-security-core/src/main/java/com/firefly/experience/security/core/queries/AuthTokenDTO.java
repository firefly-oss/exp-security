package com.firefly.experience.security.core.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authentication token response returned after a successful login or token refresh.
 * Wraps the access token, refresh token, expiry, and token type issued by the IDP.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthTokenDTO {

    /** Bearer token used to authenticate subsequent API requests. */
    private String accessToken;

    /** Long-lived token used to obtain a new access token without re-authenticating. */
    private String refreshToken;

    /** Number of seconds until the access token expires. */
    private Long expiresIn;

    /** Token type (typically {@code Bearer}). */
    private String tokenType;
}

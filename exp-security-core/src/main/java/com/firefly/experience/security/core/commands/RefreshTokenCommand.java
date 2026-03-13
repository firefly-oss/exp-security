package com.firefly.experience.security.core.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command to exchange a refresh token for a new access token.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenCommand {

    /** Valid refresh token previously issued during login. */
    private String refreshToken;
}

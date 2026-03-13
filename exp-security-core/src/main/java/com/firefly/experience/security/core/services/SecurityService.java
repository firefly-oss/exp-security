package com.firefly.experience.security.core.services;

import com.firefly.experience.security.core.commands.BiometricLoginCommand;
import com.firefly.experience.security.core.commands.LoginCommand;
import com.firefly.experience.security.core.commands.PasswordChangeCommand;
import com.firefly.experience.security.core.commands.PasswordResetCommand;
import com.firefly.experience.security.core.commands.RefreshTokenCommand;
import com.firefly.experience.security.core.commands.RequestScaChallengeCommand;
import com.firefly.experience.security.core.commands.VerifyScaCommand;
import com.firefly.experience.security.core.queries.ActivityLogEntryDTO;
import com.firefly.experience.security.core.queries.AuthTokenDTO;
import com.firefly.experience.security.core.queries.ScaChallengeDTO;
import com.firefly.experience.security.core.queries.ScaVerificationResultDTO;
import com.firefly.experience.security.core.queries.SessionDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Experience-layer security service contract.
 *
 * <p>Exposes authentication, session management, password lifecycle, and SCA operations
 * as atomic reactive endpoints consumed by upstream controllers. All calls delegate to
 * downstream domain SDKs ({@code domain-core-security-center} and {@code domain-common-sca}).
 */
public interface SecurityService {

    /**
     * Authenticates a party using username/password credentials.
     *
     * @param command login credentials and device metadata
     * @return access and refresh tokens on successful authentication
     */
    Mono<AuthTokenDTO> login(LoginCommand command);

    /**
     * Authenticates a party using a device-generated biometric token.
     *
     * @param command biometric token and device metadata
     * @return access and refresh tokens on successful authentication
     */
    Mono<AuthTokenDTO> loginBiometric(BiometricLoginCommand command);

    /**
     * Exchanges a valid refresh token for a new access token.
     *
     * @param command refresh token to exchange
     * @return refreshed access and refresh tokens
     */
    Mono<AuthTokenDTO> refreshToken(RefreshTokenCommand command);

    /**
     * Invalidates the current party session and IDP tokens.
     *
     * @return completes empty on success
     */
    Mono<Void> logout();

    /**
     * Triggers a password-reset email for the given email address.
     *
     * @param command email associated with the account
     * @return completes empty on success
     */
    Mono<Void> resetPassword(PasswordResetCommand command);

    /**
     * Changes the authenticated party's password after verifying the current one.
     *
     * @param command current and new password
     * @return completes empty on success
     */
    Mono<Void> changePassword(PasswordChangeCommand command);

    /**
     * Requests an SCA challenge for a sensitive operation.
     *
     * @param command operation type and preferred challenge mechanism
     * @return the issued SCA challenge including its expiry
     */
    Mono<ScaChallengeDTO> requestScaChallenge(RequestScaChallengeCommand command);

    /**
     * Verifies the party's response to an issued SCA challenge.
     *
     * @param command operation ID, challenge ID, and verification code
     * @return verification result including status and timestamp
     */
    Mono<ScaVerificationResultDTO> verifySca(VerifyScaCommand command);

    /**
     * Returns the list of active sessions for the authenticated party.
     *
     * @return stream of active session descriptors
     */
    Flux<SessionDTO> getActiveSessions();

    /**
     * Invalidates a specific session by its identifier.
     *
     * @param sessionId session to close
     * @return completes empty on success
     */
    Mono<Void> closeSession(UUID sessionId);

    /**
     * Returns the security activity log for the authenticated party.
     *
     * @return stream of activity log entries in reverse chronological order
     */
    Flux<ActivityLogEntryDTO> getActivityLog();
}

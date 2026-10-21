/*
 * Copyright 2025 Firefly Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.fireflysoftware.experience.security.core.services;

import io.fireflysoftware.experience.security.core.commands.LoginCommand;
import io.fireflysoftware.experience.security.core.commands.PasswordResetCommand;
import io.fireflysoftware.experience.security.core.commands.RefreshTokenCommand;
import io.fireflysoftware.experience.security.core.commands.RequestScaChallengeCommand;
import io.fireflysoftware.experience.security.core.commands.VerifyScaCommand;
import io.fireflysoftware.experience.security.core.queries.AuthTokenDTO;
import io.fireflysoftware.experience.security.core.queries.ScaChallengeDTO;
import io.fireflysoftware.experience.security.core.queries.ScaVerificationResultDTO;
import io.fireflysoftware.experience.security.core.queries.SessionDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Experience-layer security service contract.
 *
 * <p>Authentication and sessions delegate to the security center. SCA challenge and
 * verification delegate to the SCA operations that service already exposes.
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
     * Asks the security center to reset the password for the supplied account username.
     *
     * @param command account username, which is an email address when that is how the party signs in
     * @return completes empty on success
     */
    Mono<Void> resetPassword(PasswordResetCommand command);

    /**
     * Generates a challenge for an existing SCA operation.
     *
     * @param command operation id and challenge mechanism
     * @return the issued SCA challenge including its expiry
     */
    Mono<ScaChallengeDTO> requestScaChallenge(RequestScaChallengeCommand command);

    /**
     * Verifies the party's response to an issued SCA challenge.
     *
     * @param command operation ID, challenge ID, and verification code
     * @return verification result including the operation status and last update time
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
}

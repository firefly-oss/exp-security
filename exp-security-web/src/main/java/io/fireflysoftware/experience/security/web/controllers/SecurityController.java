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


package io.fireflysoftware.experience.security.web.controllers;

import io.fireflysoftware.experience.security.core.commands.LoginCommand;
import io.fireflysoftware.experience.security.core.commands.PasswordResetCommand;
import io.fireflysoftware.experience.security.core.commands.RefreshTokenCommand;
import io.fireflysoftware.experience.security.core.commands.RequestScaChallengeCommand;
import io.fireflysoftware.experience.security.core.commands.VerifyScaCommand;
import io.fireflysoftware.experience.security.core.queries.AuthTokenDTO;
import io.fireflysoftware.experience.security.core.queries.ScaChallengeDTO;
import io.fireflysoftware.experience.security.core.queries.ScaVerificationResultDTO;
import io.fireflysoftware.experience.security.core.queries.SessionDTO;
import io.fireflysoftware.experience.security.core.services.SecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for authentication, password reset, SCA, and session management.
 * Delegates each call to an operation the security center or SCA service already exposes.
 */
@Tag(name = "Security", description = "Authentication, SCA, and session management")
@RestController
@RequestMapping("/api/v1/experience/security")
@RequiredArgsConstructor
public class SecurityController {

    private final SecurityService service;

    // ─── Authentication ───────────────────────────────────────────────────────

    /**
     * Authenticates a party using username/password credentials.
     *
     * @param command login credentials and device metadata
     * @return 200 OK with access and refresh tokens on success
     */
    @Operation(summary = "Login",
               description = "Authenticates a party using username/password credentials and returns access and refresh tokens.")
    @PostMapping(value = "/auth/login",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<AuthTokenDTO>> login(@Valid @RequestBody LoginCommand command) {
        return service.login(command)
                .map(ResponseEntity::ok);
    }

    /**
     * Exchanges a valid refresh token for a new access token.
     *
     * @param command refresh token to exchange
     * @return 200 OK with refreshed access and refresh tokens
     */
    @Operation(summary = "Refresh Token",
               description = "Exchanges a valid refresh token for a new access token.")
    @PostMapping(value = "/auth/refresh",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<AuthTokenDTO>> refreshToken(@Valid @RequestBody RefreshTokenCommand command) {
        return service.refreshToken(command)
                .map(ResponseEntity::ok);
    }

    /**
     * Invalidates the current party session and IDP tokens.
     *
     * @return 204 NO_CONTENT on success
     */
    @Operation(summary = "Logout",
               description = "Invalidates the current party session and IDP tokens.")
    @PostMapping(value = "/auth/logout")
    public Mono<ResponseEntity<Void>> logout() {
        return service.logout()
                .thenReturn(ResponseEntity.noContent().<Void>build());
    }

    // ─── Password ─────────────────────────────────────────────────────────────

    /**
     * Asks the security center to reset the password for the supplied account username.
     *
     * @param command account username, which is an email address when that is how the party signs in
     * @return 202 ACCEPTED on success
     */
    @Operation(summary = "Reset Password",
               description = "Resets the password for the supplied account username through the security center.")
    @PostMapping(value = "/auth/password/reset",
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Void>> resetPassword(@Valid @RequestBody PasswordResetCommand command) {
        return service.resetPassword(command)
                .thenReturn(ResponseEntity.accepted().<Void>build());
    }

    // ─── SCA ──────────────────────────────────────────────────────────────────

    /**
     * Generates a challenge for an SCA operation that already exists.
     *
     * @param command operation id and challenge mechanism
     * @return 201 CREATED with the issued SCA challenge including its expiry
     */
    @Operation(summary = "Request SCA Challenge",
               description = "Generates a challenge for an existing SCA operation.")
    @PostMapping(value = "/sca/challenge",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ScaChallengeDTO>> requestScaChallenge(
            @Valid @RequestBody RequestScaChallengeCommand command) {
        return service.requestScaChallenge(command)
                .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto));
    }

    /**
     * Verifies the party's response to an issued SCA challenge.
     *
     * @param command operation ID, challenge ID, and verification code
     * @return 200 OK with verification result including status and timestamp
     */
    @Operation(summary = "Verify SCA",
               description = "Verifies the party's response to an issued SCA challenge.")
    @PostMapping(value = "/sca/verify",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ScaVerificationResultDTO>> verifySca(@Valid @RequestBody VerifyScaCommand command) {
        return service.verifySca(command)
                .map(ResponseEntity::ok);
    }

    // ─── Sessions ─────────────────────────────────────────────────────────────

    /**
     * Returns the list of active sessions for the authenticated party.
     *
     * @return 200 OK with the list of active session descriptors
     */
    @Operation(summary = "Get Active Sessions",
               description = "Returns the list of active sessions for the authenticated party.")
    @GetMapping(value = "/sessions", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<SessionDTO>>> getActiveSessions() {
        return service.getActiveSessions()
                .collectList()
                .map(ResponseEntity::ok);
    }

    /**
     * Invalidates a specific session by its identifier.
     *
     * @param sessionId unique identifier of the session to close
     * @return 204 NO_CONTENT on success
     */
    @Operation(summary = "Close Session",
               description = "Invalidates a specific session by its identifier.")
    @DeleteMapping(value = "/sessions/{sessionId}")
    public Mono<ResponseEntity<Void>> closeSession(
            @Parameter(description = "Unique identifier of the session to close", required = true)
            @PathVariable UUID sessionId) {
        return service.closeSession(sessionId)
                .thenReturn(ResponseEntity.noContent().<Void>build());
    }
}

package com.firefly.experience.security.web.controllers;

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
import com.firefly.experience.security.core.services.SecurityService;
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
 * REST controller exposing atomic security endpoints for authentication, SCA, session
 * management, and activity log queries. Delegates all logic to {@link SecurityService}.
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
     * Authenticates a party using a device-generated biometric token.
     *
     * @param command biometric token and device metadata
     * @return 200 OK with access and refresh tokens on success
     */
    @Operation(summary = "Biometric Login",
               description = "Authenticates a party using a device-generated biometric token.")
    @PostMapping(value = "/auth/login/biometric",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<AuthTokenDTO>> loginBiometric(@Valid @RequestBody BiometricLoginCommand command) {
        return service.loginBiometric(command)
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
     * Triggers a password-reset email for the account associated with the given email address.
     *
     * @param command email associated with the account
     * @return 202 ACCEPTED on success
     */
    @Operation(summary = "Reset Password",
               description = "Triggers a password-reset email for the account associated with the given email address.")
    @PostMapping(value = "/auth/password/reset",
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Void>> resetPassword(@Valid @RequestBody PasswordResetCommand command) {
        return service.resetPassword(command)
                .thenReturn(ResponseEntity.accepted().<Void>build());
    }

    /**
     * Changes the authenticated party's password after verifying the current one.
     *
     * @param command current and new password
     * @return 204 NO_CONTENT on success
     */
    @Operation(summary = "Change Password",
               description = "Changes the authenticated party's password after verifying the current one.")
    @PostMapping(value = "/auth/password/change",
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Void>> changePassword(@Valid @RequestBody PasswordChangeCommand command) {
        return service.changePassword(command)
                .thenReturn(ResponseEntity.noContent().<Void>build());
    }

    // ─── SCA ──────────────────────────────────────────────────────────────────

    /**
     * Issues an SCA challenge for the requested operation type and preferred mechanism.
     *
     * @param command operation type and preferred challenge mechanism
     * @return 201 CREATED with the issued SCA challenge including its expiry
     */
    @Operation(summary = "Request SCA Challenge",
               description = "Issues an SCA challenge for the requested operation type and preferred mechanism.")
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

    // ─── Activity Log ─────────────────────────────────────────────────────────

    /**
     * Returns the security activity log for the authenticated party in reverse chronological order.
     *
     * @return 200 OK with the list of activity log entries
     */
    @Operation(summary = "Get Activity Log",
               description = "Returns the security activity log for the authenticated party in reverse chronological order.")
    @GetMapping(value = "/activity-log", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<ActivityLogEntryDTO>>> getActivityLog() {
        return service.getActivityLog()
                .collectList()
                .map(ResponseEntity::ok);
    }
}

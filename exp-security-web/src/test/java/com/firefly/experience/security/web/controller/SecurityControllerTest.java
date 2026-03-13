package com.firefly.experience.security.web.controller;

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
import com.firefly.experience.security.web.controllers.SecurityController;
import org.fireflyframework.web.error.config.ErrorHandlingProperties;
import org.fireflyframework.web.error.converter.ExceptionConverterService;
import org.fireflyframework.web.error.service.ErrorResponseNegotiator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SecurityController}.
 *
 * <p>{@link SecurityService} is mocked to verify that each endpoint maps to the correct
 * service method, applies the right HTTP status code, and correctly shapes the response body.
 */
@WebFluxTest(
        controllers = SecurityController.class,
        excludeAutoConfiguration = {
                ReactiveSecurityAutoConfiguration.class,
                ReactiveUserDetailsServiceAutoConfiguration.class
        }
)
class SecurityControllerTest {

    @Autowired
    private WebTestClient webClient;

    @MockBean
    private SecurityService service;

    // Required by GlobalExceptionHandler auto-configured from fireflyframework-web
    @MockBean
    @SuppressWarnings("unused")
    private ExceptionConverterService exceptionConverterService;

    @MockBean
    @SuppressWarnings("unused")
    private ErrorHandlingProperties errorHandlingProperties;

    @MockBean
    @SuppressWarnings("unused")
    private ErrorResponseNegotiator errorResponseNegotiator;

    // ─── Authentication ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /auth/login")
    class LoginTests {

        @Test
        @DisplayName("returns 200 OK with AuthTokenDTO on successful login")
        void login_returns200WithToken() {
            var token = AuthTokenDTO.builder()
                    .accessToken("access-abc")
                    .refreshToken("refresh-xyz")
                    .expiresIn(3600L)
                    .tokenType("Bearer")
                    .build();

            when(service.login(any())).thenReturn(Mono.just(token));

            webClient.post().uri("/api/v1/experience/security/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(LoginCommand.builder().username("alice").password("s3cr3t").build())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(AuthTokenDTO.class)
                    .value(body -> {
                        assertThat(body.getAccessToken()).isEqualTo("access-abc");
                        assertThat(body.getTokenType()).isEqualTo("Bearer");
                    });

            verify(service).login(any());
        }
    }

    @Nested
    @DisplayName("POST /auth/login/biometric")
    class BiometricLoginTests {

        @Test
        @DisplayName("returns 200 OK with AuthTokenDTO on successful biometric login")
        void loginBiometric_returns200WithToken() {
            var token = AuthTokenDTO.builder()
                    .accessToken("bio-access")
                    .refreshToken("bio-refresh")
                    .expiresIn(3600L)
                    .tokenType("Bearer")
                    .build();

            when(service.loginBiometric(any())).thenReturn(Mono.just(token));

            webClient.post().uri("/api/v1/experience/security/auth/login/biometric")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(BiometricLoginCommand.builder().biometricToken("bio-tok").build())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(AuthTokenDTO.class)
                    .value(body -> assertThat(body.getAccessToken()).isEqualTo("bio-access"));

            verify(service).loginBiometric(any());
        }
    }

    @Nested
    @DisplayName("POST /auth/refresh")
    class RefreshTokenTests {

        @Test
        @DisplayName("returns 200 OK with refreshed AuthTokenDTO")
        void refreshToken_returns200WithNewToken() {
            var token = AuthTokenDTO.builder()
                    .accessToken("new-access")
                    .refreshToken("new-refresh")
                    .expiresIn(3600L)
                    .tokenType("Bearer")
                    .build();

            when(service.refreshToken(any())).thenReturn(Mono.just(token));

            webClient.post().uri("/api/v1/experience/security/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new RefreshTokenCommand("old-refresh"))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(AuthTokenDTO.class)
                    .value(body -> assertThat(body.getAccessToken()).isEqualTo("new-access"));

            verify(service).refreshToken(any());
        }
    }

    @Nested
    @DisplayName("POST /auth/logout")
    class LogoutTests {

        @Test
        @DisplayName("returns 204 NO_CONTENT and delegates to service.logout()")
        void logout_returns204() {
            when(service.logout()).thenReturn(Mono.empty());

            webClient.post().uri("/api/v1/experience/security/auth/logout")
                    .exchange()
                    .expectStatus().isNoContent();

            verify(service).logout();
        }
    }

    // ─── Password ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /auth/password/reset")
    class ResetPasswordTests {

        @Test
        @DisplayName("returns 202 ACCEPTED and delegates to service.resetPassword()")
        void resetPassword_returns202() {
            when(service.resetPassword(any())).thenReturn(Mono.empty());

            webClient.post().uri("/api/v1/experience/security/auth/password/reset")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new PasswordResetCommand("alice@example.com"))
                    .exchange()
                    .expectStatus().isAccepted();

            verify(service).resetPassword(any());
        }
    }

    @Nested
    @DisplayName("POST /auth/password/change")
    class ChangePasswordTests {

        @Test
        @DisplayName("returns 204 NO_CONTENT and delegates to service.changePassword()")
        void changePassword_returns204() {
            when(service.changePassword(any())).thenReturn(Mono.empty());

            webClient.post().uri("/api/v1/experience/security/auth/password/change")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new PasswordChangeCommand("old-pass", "new-pass"))
                    .exchange()
                    .expectStatus().isNoContent();

            verify(service).changePassword(any());
        }
    }

    // ─── SCA ──────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /sca/challenge")
    class RequestScaChallengeTests {

        @Test
        @DisplayName("returns 201 CREATED with ScaChallengeDTO")
        void requestScaChallenge_returns201() {
            var challengeId = UUID.randomUUID();
            var operationId = UUID.randomUUID();
            var challenge = ScaChallengeDTO.builder()
                    .challengeId(challengeId)
                    .operationId(operationId)
                    .challengeType("OTP")
                    .expiresAt(LocalDateTime.now().plusMinutes(5))
                    .build();

            when(service.requestScaChallenge(any())).thenReturn(Mono.just(challenge));

            webClient.post().uri("/api/v1/experience/security/sca/challenge")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new RequestScaChallengeCommand("TRANSFER", "OTP"))
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(ScaChallengeDTO.class)
                    .value(body -> {
                        assertThat(body.getChallengeId()).isEqualTo(challengeId);
                        assertThat(body.getChallengeType()).isEqualTo("OTP");
                    });

            verify(service).requestScaChallenge(any());
        }
    }

    @Nested
    @DisplayName("POST /sca/verify")
    class VerifyScaTests {

        @Test
        @DisplayName("returns 200 OK with ScaVerificationResultDTO")
        void verifySca_returns200() {
            var operationId = UUID.randomUUID();
            var result = ScaVerificationResultDTO.builder()
                    .operationId(operationId)
                    .status("VERIFIED")
                    .verifiedAt(LocalDateTime.now())
                    .build();

            when(service.verifySca(any())).thenReturn(Mono.just(result));

            webClient.post().uri("/api/v1/experience/security/sca/verify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new VerifyScaCommand(operationId, UUID.randomUUID(), "123456"))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(ScaVerificationResultDTO.class)
                    .value(body -> {
                        assertThat(body.getOperationId()).isEqualTo(operationId);
                        assertThat(body.getStatus()).isEqualTo("VERIFIED");
                    });

            verify(service).verifySca(any());
        }
    }

    // ─── Sessions ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /sessions")
    class GetActiveSessionsTests {

        @Test
        @DisplayName("returns 200 OK with list of SessionDTOs")
        void getActiveSessions_returns200WithList() {
            var sessionId = UUID.randomUUID();
            var session = SessionDTO.builder()
                    .sessionId(sessionId)
                    .deviceInfo("iPhone 15")
                    .ipAddress("10.0.0.1")
                    .createdAt(LocalDateTime.now())
                    .build();

            when(service.getActiveSessions()).thenReturn(Flux.just(session));

            webClient.get().uri("/api/v1/experience/security/sessions")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(SessionDTO.class)
                    .value(list -> {
                        assertThat(list).hasSize(1);
                        assertThat(list.get(0).getSessionId()).isEqualTo(sessionId);
                        assertThat(list.get(0).getDeviceInfo()).isEqualTo("iPhone 15");
                    });

            verify(service).getActiveSessions();
        }

        @Test
        @DisplayName("returns 200 OK with empty list when no active sessions")
        void getActiveSessions_returns200WithEmptyList() {
            when(service.getActiveSessions()).thenReturn(Flux.empty());

            webClient.get().uri("/api/v1/experience/security/sessions")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(SessionDTO.class)
                    .value(list -> assertThat(list).isEmpty());
        }
    }

    @Nested
    @DisplayName("DELETE /sessions/{sessionId}")
    class CloseSessionTests {

        @Test
        @DisplayName("returns 204 NO_CONTENT and delegates closeSession() with the given sessionId")
        void closeSession_returns204() {
            var sessionId = UUID.randomUUID();
            when(service.closeSession(eq(sessionId))).thenReturn(Mono.empty());

            webClient.delete().uri("/api/v1/experience/security/sessions/{id}", sessionId)
                    .exchange()
                    .expectStatus().isNoContent();

            verify(service).closeSession(sessionId);
        }
    }

    // ─── Activity Log ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /activity-log")
    class GetActivityLogTests {

        @Test
        @DisplayName("returns 200 OK with list of ActivityLogEntryDTOs")
        void getActivityLog_returns200WithList() {
            var entryId = UUID.randomUUID();
            var entry = ActivityLogEntryDTO.builder()
                    .id(entryId)
                    .action("LOGIN")
                    .timestamp(LocalDateTime.now())
                    .ipAddress("10.0.0.1")
                    .result("SUCCESS")
                    .build();

            when(service.getActivityLog()).thenReturn(Flux.just(entry));

            webClient.get().uri("/api/v1/experience/security/activity-log")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(ActivityLogEntryDTO.class)
                    .value(list -> {
                        assertThat(list).hasSize(1);
                        assertThat(list.get(0).getId()).isEqualTo(entryId);
                        assertThat(list.get(0).getAction()).isEqualTo("LOGIN");
                    });

            verify(service).getActivityLog();
        }

        @Test
        @DisplayName("returns 200 OK with empty list when log is empty")
        void getActivityLog_returns200WithEmptyList() {
            when(service.getActivityLog()).thenReturn(Flux.empty());

            webClient.get().uri("/api/v1/experience/security/activity-log")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(ActivityLogEntryDTO.class)
                    .value(list -> assertThat(list).isEmpty());
        }
    }
}

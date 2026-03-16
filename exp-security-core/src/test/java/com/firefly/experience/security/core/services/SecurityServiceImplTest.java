package com.firefly.experience.security.core.services;

import com.firefly.experience.security.core.commands.BiometricLoginCommand;
import com.firefly.experience.security.core.commands.LoginCommand;
import com.firefly.experience.security.core.commands.PasswordChangeCommand;
import com.firefly.experience.security.core.commands.PasswordResetCommand;
import com.firefly.experience.security.core.commands.RefreshTokenCommand;
import com.firefly.experience.security.core.commands.RequestScaChallengeCommand;
import com.firefly.experience.security.core.commands.VerifyScaCommand;
import com.firefly.experience.security.core.mappers.SecurityMapper;
import com.firefly.experience.security.core.queries.AuthTokenDTO;
import com.firefly.experience.security.core.queries.SessionDTO;
import com.firefly.experience.security.core.services.impl.SecurityServiceImpl;
import com.firefly.security.center.sdk.api.AuthenticationControllerApi;
import com.firefly.security.center.sdk.api.SessionControllerApi;
import com.firefly.security.center.sdk.model.AuthenticationResponse;
import com.firefly.security.center.sdk.model.SessionContextDTO;
import org.fireflyframework.web.error.exceptions.NotImplementedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SecurityServiceImpl}.
 *
 * <p>SDK APIs ({@link AuthenticationControllerApi}, {@link SessionControllerApi}) and the {@link SecurityMapper}
 * are mocked to isolate service logic and verify delegation behaviour without starting a
 * Spring context or making real HTTP calls.
 */
@ExtendWith(MockitoExtension.class)
class SecurityServiceImplTest {

    @Mock
    private AuthenticationControllerApi authenticationApi;

    @Mock
    private SessionControllerApi sessionsApi;

    @Mock
    private SecurityMapper mapper;

    private SecurityServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SecurityServiceImpl(authenticationApi, sessionsApi, mapper);
    }

    // ─── Authentication ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("login()")
    class LoginTests {

        @Test
        @DisplayName("maps command to LoginRequest, delegates to authenticationApi, maps response to AuthTokenDTO")
        void login_happyPath() {
            var command = LoginCommand.builder()
                    .username("alice@example.com")
                    .password("s3cr3t")
                    .deviceInfo("iPhone 15")
                    .build();

            var sdkResponse = new AuthenticationResponse()
                    .accessToken("access-123")
                    .refreshToken("refresh-456")
                    .expiresIn(3600L)
                    .tokenType("Bearer");

            var expected = AuthTokenDTO.builder()
                    .accessToken("access-123")
                    .refreshToken("refresh-456")
                    .expiresIn(3600L)
                    .tokenType("Bearer")
                    .build();

            when(authenticationApi.login(any(), any())).thenReturn(Mono.just(sdkResponse));
            when(mapper.toAuthTokenDTO(sdkResponse)).thenReturn(expected);

            StepVerifier.create(service.login(command))
                    .expectNext(expected)
                    .verifyComplete();

            var captor = ArgumentCaptor.forClass(com.firefly.security.center.sdk.model.LoginRequest.class);
            verify(authenticationApi).login(captor.capture(), any());
            assertThat(captor.getValue().getUsername()).isEqualTo("alice@example.com");
            assertThat(captor.getValue().getPassword()).isEqualTo("s3cr3t");
        }

        @Test
        @DisplayName("propagates error when authenticationApi.login() fails")
        void login_propagatesApiError() {
            var command = LoginCommand.builder().username("bob").password("wrong").build();
            when(authenticationApi.login(any(), any()))
                    .thenReturn(Mono.error(new RuntimeException("Unauthorized")));

            StepVerifier.create(service.login(command))
                    .expectError(RuntimeException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("loginBiometric()")
    class BiometricLoginTests {

        @Test
        @DisplayName("emits NotImplementedException — biometric endpoint not yet available")
        void loginBiometric_notImplemented() {
            var command = BiometricLoginCommand.builder()
                    .biometricToken("bio-token")
                    .deviceInfo("iPhone 15")
                    .build();

            StepVerifier.create(service.loginBiometric(command))
                    .expectError(NotImplementedException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("refreshToken()")
    class RefreshTokenTests {

        @Test
        @DisplayName("maps command to RefreshRequest, delegates to authenticationApi.refresh(), maps response")
        void refreshToken_happyPath() {
            var command = new RefreshTokenCommand("refresh-old");

            var sdkResponse = new AuthenticationResponse()
                    .accessToken("access-new")
                    .refreshToken("refresh-new")
                    .expiresIn(3600L)
                    .tokenType("Bearer");

            var expected = AuthTokenDTO.builder()
                    .accessToken("access-new")
                    .refreshToken("refresh-new")
                    .build();

            when(authenticationApi.refresh(any(), any())).thenReturn(Mono.just(sdkResponse));
            when(mapper.toAuthTokenDTO(sdkResponse)).thenReturn(expected);

            StepVerifier.create(service.refreshToken(command))
                    .expectNext(expected)
                    .verifyComplete();

            var captor = ArgumentCaptor.forClass(com.firefly.security.center.sdk.model.RefreshRequest.class);
            verify(authenticationApi).refresh(captor.capture(), any());
            assertThat(captor.getValue().getRefreshToken()).isEqualTo("refresh-old");
        }
    }

    @Nested
    @DisplayName("logout()")
    class LogoutTests {

        @Test
        @DisplayName("delegates to authenticationApi.logout()")
        void logout_delegatesToApi() {
            when(authenticationApi.logout(any(), any())).thenReturn(Mono.empty());

            StepVerifier.create(service.logout())
                    .verifyComplete();

            verify(authenticationApi).logout(any(), any());
        }
    }

    // ─── Password ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("resetPassword()")
    class ResetPasswordTests {

        @Test
        @DisplayName("emits NotImplementedException — password reset endpoint not yet available")
        void resetPassword_notImplemented() {
            var command = new PasswordResetCommand("alice@example.com");

            StepVerifier.create(service.resetPassword(command))
                    .expectError(NotImplementedException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("changePassword()")
    class ChangePasswordTests {

        @Test
        @DisplayName("emits NotImplementedException — password change endpoint not yet available")
        void changePassword_notImplemented() {
            var command = new PasswordChangeCommand("old-pass", "new-pass");

            StepVerifier.create(service.changePassword(command))
                    .expectError(NotImplementedException.class)
                    .verify();
        }
    }

    // ─── SCA ──────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("requestScaChallenge()")
    class RequestScaChallengeTests {

        @Test
        @DisplayName("emits NotImplementedException — SCA SDK not yet generated")
        void requestScaChallenge_notImplemented() {
            var command = new RequestScaChallengeCommand("TRANSFER", "OTP");

            StepVerifier.create(service.requestScaChallenge(command))
                    .expectError(NotImplementedException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("verifySca()")
    class VerifyScaTests {

        @Test
        @DisplayName("emits NotImplementedException — SCA SDK not yet generated")
        void verifySca_notImplemented() {
            var command = new VerifyScaCommand(UUID.randomUUID(), UUID.randomUUID(), "123456");

            StepVerifier.create(service.verifySca(command))
                    .expectError(NotImplementedException.class)
                    .verify();
        }
    }

    // ─── Sessions ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getActiveSessions()")
    class GetActiveSessionsTests {

        @Test
        @DisplayName("maps SessionContextDTO from createOrGetSession() to SessionDTO")
        void getActiveSessions_returnsCurrentSession() {
            var sessionId = UUID.randomUUID();
            var sdkSession = new SessionContextDTO()
                    .sessionId(sessionId.toString())
                    .ipAddress("192.168.1.1")
                    .userAgent("Mozilla/5.0")
                    .createdAt(LocalDateTime.now());

            var expected = SessionDTO.builder()
                    .sessionId(sessionId)
                    .ipAddress("192.168.1.1")
                    .deviceInfo("Mozilla/5.0")
                    .build();

            when(sessionsApi.createOrGetSession(any())).thenReturn(Mono.just(sdkSession));
            when(mapper.toSessionDTO(sdkSession)).thenReturn(expected);

            StepVerifier.create(service.getActiveSessions())
                    .expectNext(expected)
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("closeSession()")
    class CloseSessionTests {

        @Test
        @DisplayName("delegates to sessionsApi.invalidateSession() with sessionId as string")
        void closeSession_delegatesToApi() {
            var sessionId = UUID.randomUUID();
            when(sessionsApi.invalidateSession(eq(sessionId.toString()), any())).thenReturn(Mono.empty());

            StepVerifier.create(service.closeSession(sessionId))
                    .verifyComplete();

            verify(sessionsApi).invalidateSession(eq(sessionId.toString()), any());
        }
    }

    // ─── Activity Log ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getActivityLog()")
    class GetActivityLogTests {

        @Test
        @DisplayName("emits NotImplementedException — activity log endpoint not yet available")
        void getActivityLog_notImplemented() {
            StepVerifier.create(service.getActivityLog())
                    .expectError(NotImplementedException.class)
                    .verify();
        }
    }
}

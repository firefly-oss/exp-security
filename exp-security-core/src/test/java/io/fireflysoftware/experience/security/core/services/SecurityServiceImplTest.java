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

import io.fireflysoftware.domain.common.sca.sdk.api.ScaOperationsApi;
import io.fireflysoftware.domain.common.sca.sdk.model.GenerateChallengeRequest;
import io.fireflysoftware.domain.common.sca.sdk.model.SCAChallengeDTO;
import io.fireflysoftware.domain.common.sca.sdk.model.SCAOperationDTO;
import io.fireflysoftware.domain.common.sca.sdk.model.SagaResult;
import io.fireflysoftware.domain.common.sca.sdk.model.VerifyAttemptRequest;
import io.fireflysoftware.experience.security.core.commands.LoginCommand;
import io.fireflysoftware.experience.security.core.commands.PasswordResetCommand;
import io.fireflysoftware.experience.security.core.commands.RefreshTokenCommand;
import io.fireflysoftware.experience.security.core.commands.RequestScaChallengeCommand;
import io.fireflysoftware.experience.security.core.commands.VerifyScaCommand;
import io.fireflysoftware.experience.security.core.mappers.SecurityMapper;
import io.fireflysoftware.experience.security.core.queries.AuthTokenDTO;
import io.fireflysoftware.experience.security.core.queries.SessionDTO;
import io.fireflysoftware.experience.security.core.services.impl.SecurityServiceImpl;
import io.fireflysoftware.security.center.sdk.api.AuthenticationControllerApi;
import io.fireflysoftware.security.center.sdk.api.SessionControllerApi;
import io.fireflysoftware.security.center.sdk.model.AuthenticationResponse;
import io.fireflysoftware.security.center.sdk.model.SessionContextDTO;
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
 */
@ExtendWith(MockitoExtension.class)
class SecurityServiceImplTest {

    @Mock
    private AuthenticationControllerApi authenticationApi;

    @Mock
    private SessionControllerApi sessionsApi;

    @Mock
    private ScaOperationsApi scaOperationsApi;

    @Mock
    private SecurityMapper mapper;

    private SecurityServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SecurityServiceImpl(authenticationApi, sessionsApi, scaOperationsApi, mapper);
    }

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

            var captor = ArgumentCaptor.forClass(io.fireflysoftware.security.center.sdk.model.LoginRequest.class);
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

            var captor = ArgumentCaptor.forClass(io.fireflysoftware.security.center.sdk.model.RefreshRequest.class);
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

    @Nested
    @DisplayName("resetPassword()")
    class ResetPasswordTests {

        @Test
        @DisplayName("sends the account identifier to the security center reset operation")
        void resetPassword_delegatesToSecurityCenter() {
            when(authenticationApi.resetPassword(eq("alice@example.com"), any())).thenReturn(Mono.empty());

            StepVerifier.create(service.resetPassword(new PasswordResetCommand("alice@example.com")))
                    .verifyComplete();

            verify(authenticationApi).resetPassword(eq("alice@example.com"), any());
        }
    }

    @Nested
    @DisplayName("requestScaChallenge()")
    class RequestScaChallengeTests {

        @Test
        @DisplayName("generates a challenge for the existing SCA operation")
        void requestScaChallenge_delegatesToSca() {
            UUID operationId = UUID.randomUUID();
            UUID challengeId = UUID.randomUUID();
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);
            var command = new RequestScaChallengeCommand(operationId, "OTP");
            var issued = new SCAChallengeDTO()
                    .id(challengeId)
                    .scaOperationId(operationId)
                    .expiresAt(expiresAt);

            when(scaOperationsApi.generateChallenge(eq(operationId), any(GenerateChallengeRequest.class), any()))
                    .thenReturn(Mono.just(issued));

            StepVerifier.create(service.requestScaChallenge(command))
                    .assertNext(dto -> {
                        assertThat(dto.getChallengeId()).isEqualTo(challengeId);
                        assertThat(dto.getOperationId()).isEqualTo(operationId);
                        assertThat(dto.getChallengeType()).isEqualTo("OTP");
                        assertThat(dto.getExpiresAt()).isEqualTo(expiresAt);
                    })
                    .verifyComplete();

            var captor = ArgumentCaptor.forClass(GenerateChallengeRequest.class);
            verify(scaOperationsApi).generateChallenge(eq(operationId), captor.capture(), any());
            assertThat(captor.getValue().getChallengeType())
                    .isEqualTo(GenerateChallengeRequest.ChallengeTypeEnum.OTP);
        }
    }

    @Nested
    @DisplayName("verifySca()")
    class VerifyScaTests {

        @Test
        @DisplayName("verifies the attempt and returns the operation status from SCA")
        void verifySca_readsStatusAfterVerification() {
            UUID operationId = UUID.randomUUID();
            UUID challengeId = UUID.randomUUID();
            LocalDateTime updated = LocalDateTime.now();
            var command = new VerifyScaCommand(operationId, challengeId, "123456");
            var operation = new SCAOperationDTO()
                    .id(operationId)
                    .status(SCAOperationDTO.StatusEnum.VERIFIED)
                    .lastUpdated(updated);

            when(scaOperationsApi.verifyAttempt(eq(operationId), any(VerifyAttemptRequest.class), any()))
                    .thenReturn(Mono.just(new SagaResult().success(true).failed(false)));
            when(scaOperationsApi.getOperation(eq(operationId), any())).thenReturn(Mono.just(operation));

            StepVerifier.create(service.verifySca(command))
                    .assertNext(result -> {
                        assertThat(result.getOperationId()).isEqualTo(operationId);
                        assertThat(result.getStatus()).isEqualTo("VERIFIED");
                        assertThat(result.getVerifiedAt()).isEqualTo(updated);
                    })
                    .verifyComplete();

            var captor = ArgumentCaptor.forClass(VerifyAttemptRequest.class);
            verify(scaOperationsApi).verifyAttempt(eq(operationId), captor.capture(), any());
            assertThat(captor.getValue().getChallengeId()).isEqualTo(challengeId);
            assertThat(captor.getValue().getVerificationCode()).isEqualTo("123456");
        }
    }

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
}

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


package io.fireflysoftware.experience.security.core.services.impl;

import io.fireflysoftware.domain.common.sca.sdk.api.ScaOperationsApi;
import io.fireflysoftware.domain.common.sca.sdk.model.GenerateChallengeRequest;
import io.fireflysoftware.domain.common.sca.sdk.model.VerifyAttemptRequest;
import io.fireflysoftware.experience.security.core.commands.LoginCommand;
import io.fireflysoftware.experience.security.core.commands.PasswordResetCommand;
import io.fireflysoftware.experience.security.core.commands.RefreshTokenCommand;
import io.fireflysoftware.experience.security.core.commands.RequestScaChallengeCommand;
import io.fireflysoftware.experience.security.core.commands.VerifyScaCommand;
import io.fireflysoftware.experience.security.core.mappers.SecurityMapper;
import io.fireflysoftware.experience.security.core.queries.AuthTokenDTO;
import io.fireflysoftware.experience.security.core.queries.ScaChallengeDTO;
import io.fireflysoftware.experience.security.core.queries.ScaVerificationResultDTO;
import io.fireflysoftware.experience.security.core.queries.SessionDTO;
import io.fireflysoftware.experience.security.core.services.SecurityService;
import io.fireflysoftware.security.center.sdk.api.AuthenticationControllerApi;
import io.fireflysoftware.security.center.sdk.api.SessionControllerApi;
import io.fireflysoftware.security.center.sdk.model.AuthLogoutRequest;
import io.fireflysoftware.security.center.sdk.model.LoginRequest;
import io.fireflysoftware.security.center.sdk.model.RefreshRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Delegates authentication and sessions to the security center, and SCA challenge
 * generation and verification to the SCA operations API.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityServiceImpl implements SecurityService {

    private final AuthenticationControllerApi authenticationApi;
    private final SessionControllerApi sessionsApi;
    private final ScaOperationsApi scaOperationsApi;
    private final SecurityMapper mapper;

    @Override
    public Mono<AuthTokenDTO> login(LoginCommand command) {
        log.debug("Initiating login");
        LoginRequest request = new LoginRequest()
                .username(command.getUsername())
                .password(command.getPassword());
        return authenticationApi.login(request, UUID.randomUUID().toString())
                .map(mapper::toAuthTokenDTO);
    }

    @Override
    public Mono<AuthTokenDTO> refreshToken(RefreshTokenCommand command) {
        log.debug("Refreshing token");
        RefreshRequest request = new RefreshRequest()
                .refreshToken(command.getRefreshToken());
        return authenticationApi.refresh(request, UUID.randomUUID().toString())
                .map(mapper::toAuthTokenDTO);
    }

    @Override
    public Mono<Void> logout() {
        log.debug("Processing logout");
        return authenticationApi.logout(new AuthLogoutRequest(), UUID.randomUUID().toString());
    }

    @Override
    public Mono<Void> resetPassword(PasswordResetCommand command) {
        log.debug("Resetting password for account {}", command.getEmail());
        return authenticationApi.resetPassword(command.getEmail(), UUID.randomUUID().toString());
    }

    @Override
    public Mono<ScaChallengeDTO> requestScaChallenge(RequestScaChallengeCommand command) {
        log.debug("Generating SCA challenge for operation {}", command.getOperationId());
        GenerateChallengeRequest request = new GenerateChallengeRequest()
                .challengeType(GenerateChallengeRequest.ChallengeTypeEnum.fromValue(command.getChallengeType()));
        return scaOperationsApi.generateChallenge(command.getOperationId(), request, UUID.randomUUID().toString())
                .map(challenge -> ScaChallengeDTO.builder()
                        .challengeId(challenge.getId())
                        .operationId(challenge.getScaOperationId() != null
                                ? challenge.getScaOperationId()
                                : command.getOperationId())
                        .challengeType(command.getChallengeType())
                        .expiresAt(challenge.getExpiresAt())
                        .build());
    }

    @Override
    public Mono<ScaVerificationResultDTO> verifySca(VerifyScaCommand command) {
        log.debug("Verifying SCA attempt for operation {}", command.getOperationId());
        VerifyAttemptRequest request = new VerifyAttemptRequest()
                .challengeId(command.getChallengeId())
                .verificationCode(command.getVerificationCode());
        return scaOperationsApi.verifyAttempt(command.getOperationId(), request, UUID.randomUUID().toString())
                .then(scaOperationsApi.getOperation(command.getOperationId(), UUID.randomUUID().toString()))
                .map(operation -> ScaVerificationResultDTO.builder()
                        .operationId(command.getOperationId())
                        .status(operation.getStatus() == null ? null : operation.getStatus().getValue())
                        .verifiedAt(operation.getLastUpdated())
                        .build());
    }

    @Override
    public Flux<SessionDTO> getActiveSessions() {
        log.debug("Retrieving active sessions");
        return sessionsApi.createOrGetSession(UUID.randomUUID().toString())
                .map(mapper::toSessionDTO)
                .flux();
    }

    @Override
    public Mono<Void> closeSession(UUID sessionId) {
        log.debug("Closing sessionId={}", sessionId);
        return sessionsApi.invalidateSession(sessionId.toString(), UUID.randomUUID().toString());
    }
}

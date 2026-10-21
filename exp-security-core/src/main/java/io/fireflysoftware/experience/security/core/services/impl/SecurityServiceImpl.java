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

import io.fireflysoftware.experience.security.core.commands.BiometricLoginCommand;
import io.fireflysoftware.experience.security.core.commands.LoginCommand;
import io.fireflysoftware.experience.security.core.commands.PasswordChangeCommand;
import io.fireflysoftware.experience.security.core.commands.PasswordResetCommand;
import io.fireflysoftware.experience.security.core.commands.RefreshTokenCommand;
import io.fireflysoftware.experience.security.core.commands.RequestScaChallengeCommand;
import io.fireflysoftware.experience.security.core.commands.VerifyScaCommand;
import io.fireflysoftware.experience.security.core.mappers.SecurityMapper;
import io.fireflysoftware.experience.security.core.queries.ActivityLogEntryDTO;
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
import org.fireflyframework.web.error.exceptions.NotImplementedException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Implementation of {@link SecurityService} that delegates to the Security Center SDK.
 *
 * <p>Currently backed by {@link AuthenticationControllerApi} and {@link SessionControllerApi} from
 * {@code domain-core-security-center-sdk}. Methods that require APIs not yet available
 * ({@code ActivityLogApi}, {@code PasswordApi}, {@code ScaOperationsApi}) signal a
 * {@link NotImplementedException} until the corresponding SDKs are generated.
 *
 * <p>TODO: Wire {@code ActivityLogApi} once the security-center spec exposes activity log endpoints.
 * <p>TODO: Wire {@code PasswordApi} once the security-center spec exposes password endpoints.
 * <p>TODO: Uncomment {@code ScaClientFactory} in {@code exp-security-infra} and wire
 *          {@code ScaOperationsApi} once {@code domain-common-sca-sdk} is generated.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityServiceImpl implements SecurityService {

    /** Error code used when biometric login is not yet supported. */
    public static final String ERR_BIOMETRIC_LOGIN = "biometric-login";

    /** Error code used when password reset is not yet supported. */
    public static final String ERR_PASSWORD_RESET = "password-reset";

    /** Error code used when password change is not yet supported. */
    public static final String ERR_PASSWORD_CHANGE = "password-change";

    /** Error code used when SCA challenge request is not yet supported. */
    public static final String ERR_SCA_CHALLENGE_REQUEST = "sca-challenge-request";

    /** Error code used when SCA verification is not yet supported. */
    public static final String ERR_SCA_VERIFICATION = "sca-verification";

    /** Error code used when activity log retrieval is not yet supported. */
    public static final String ERR_ACTIVITY_LOG = "activity-log";

    private final AuthenticationControllerApi authenticationApi;
    private final SessionControllerApi sessionsApi;
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
    public Mono<AuthTokenDTO> loginBiometric(BiometricLoginCommand command) {
        // TODO: Implement once domain-core-security-center exposes a biometric authentication endpoint.
        return Mono.error(new NotImplementedException("Biometric login is not yet supported", ERR_BIOMETRIC_LOGIN));
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
        // AuthLogoutRequest tokens will be injected by the gateway/security context in a full deployment.
        return authenticationApi.logout(new AuthLogoutRequest(), UUID.randomUUID().toString());
    }

    @Override
    public Mono<Void> resetPassword(PasswordResetCommand command) {
        // TODO: Implement once domain-core-security-center exposes a password-reset endpoint.
        return Mono.error(new NotImplementedException("Password reset is not yet supported", ERR_PASSWORD_RESET));
    }

    @Override
    public Mono<Void> changePassword(PasswordChangeCommand command) {
        // TODO: Implement once domain-core-security-center exposes a password-change endpoint.
        return Mono.error(new NotImplementedException("Password change is not yet supported", ERR_PASSWORD_CHANGE));
    }

    @Override
    public Mono<ScaChallengeDTO> requestScaChallenge(RequestScaChallengeCommand command) {
        // TODO: Implement once domain-common-sca-sdk is generated and ScaClientFactory is uncommented.
        return Mono.error(new NotImplementedException("SCA challenge request is not yet supported", ERR_SCA_CHALLENGE_REQUEST));
    }

    @Override
    public Mono<ScaVerificationResultDTO> verifySca(VerifyScaCommand command) {
        // TODO: Implement once domain-common-sca-sdk is generated and ScaClientFactory is uncommented.
        return Mono.error(new NotImplementedException("SCA verification is not yet supported", ERR_SCA_VERIFICATION));
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

    @Override
    public Flux<ActivityLogEntryDTO> getActivityLog() {
        // TODO: Implement once domain-core-security-center exposes an activity-log endpoint.
        return Flux.error(new NotImplementedException("Activity log is not yet supported", ERR_ACTIVITY_LOG));
    }
}

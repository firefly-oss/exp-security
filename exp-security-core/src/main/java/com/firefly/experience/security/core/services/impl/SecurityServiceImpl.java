package com.firefly.experience.security.core.services.impl;

import com.firefly.experience.security.core.commands.BiometricLoginCommand;
import com.firefly.experience.security.core.commands.LoginCommand;
import com.firefly.experience.security.core.commands.PasswordChangeCommand;
import com.firefly.experience.security.core.commands.PasswordResetCommand;
import com.firefly.experience.security.core.commands.RefreshTokenCommand;
import com.firefly.experience.security.core.commands.RequestScaChallengeCommand;
import com.firefly.experience.security.core.commands.VerifyScaCommand;
import com.firefly.experience.security.core.mappers.SecurityMapper;
import com.firefly.experience.security.core.queries.ActivityLogEntryDTO;
import com.firefly.experience.security.core.queries.AuthTokenDTO;
import com.firefly.experience.security.core.queries.ScaChallengeDTO;
import com.firefly.experience.security.core.queries.ScaVerificationResultDTO;
import com.firefly.experience.security.core.queries.SessionDTO;
import com.firefly.experience.security.core.services.SecurityService;
import com.firefly.security.center.sdk.api.AuthenticationApi;
import com.firefly.security.center.sdk.api.SessionsApi;
import com.firefly.security.center.sdk.model.LoginRequest;
import com.firefly.security.center.sdk.model.LogoutRequest;
import com.firefly.security.center.sdk.model.RefreshRequest;
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
 * <p>Currently backed by {@link AuthenticationApi} and {@link SessionsApi} from
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

    private final AuthenticationApi authenticationApi;
    private final SessionsApi sessionsApi;
    private final SecurityMapper mapper;

    @Override
    public Mono<AuthTokenDTO> login(LoginCommand command) {
        log.debug("Initiating login");
        LoginRequest request = new LoginRequest()
                .username(command.getUsername())
                .password(command.getPassword());
        return authenticationApi.login(request)
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
        return authenticationApi.refresh(request)
                .map(mapper::toAuthTokenDTO);
    }

    @Override
    public Mono<Void> logout() {
        log.debug("Processing logout");
        // LogoutRequest tokens will be injected by the gateway/security context in a full deployment.
        return authenticationApi.logout(new LogoutRequest());
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
        return sessionsApi.createOrGetSession()
                .map(mapper::toSessionDTO)
                .flux();
    }

    @Override
    public Mono<Void> closeSession(UUID sessionId) {
        log.debug("Closing sessionId={}", sessionId);
        return sessionsApi.invalidateSession(sessionId.toString());
    }

    @Override
    public Flux<ActivityLogEntryDTO> getActivityLog() {
        // TODO: Implement once domain-core-security-center exposes an activity-log endpoint.
        return Flux.error(new NotImplementedException("Activity log is not yet supported", ERR_ACTIVITY_LOG));
    }
}

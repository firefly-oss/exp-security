package com.firefly.experience.security.infra;

import com.firefly.security.center.sdk.api.AuthenticationControllerApi;
import com.firefly.security.center.sdk.api.SessionControllerApi;
import com.firefly.security.center.sdk.invoker.ApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Factory that creates and configures the Security Center SDK {@link ApiClient}
 * and exposes domain API beans for dependency injection.
 *
 * <p>Current SDK ({@code domain-core-security-center-sdk}) exposes:
 * <ul>
 *   <li>{@link AuthenticationControllerApi} — login, token refresh, logout, introspect</li>
 *   <li>{@link SessionControllerApi} — active sessions, close session, session validation</li>
 * </ul>
 *
 * <p>TODO: Once the security-center OpenAPI spec is extended with Activity Log and
 * Password endpoints, regenerate the SDK and add {@code ActivityLogApi} and
 * {@code PasswordApi} beans here.
 */
@Component
public class SecurityCenterClientFactory {

    private final ApiClient apiClient;

    /**
     * Initialises the API client with the base path from configuration properties.
     *
     * @param properties connection properties for the Security Center service
     */
    public SecurityCenterClientFactory(SecurityCenterProperties properties) {
        this.apiClient = new ApiClient();
        this.apiClient.setBasePath(properties.getBasePath());
    }

    /**
     * Provides the {@link AuthenticationControllerApi} bean for login, refresh, and logout operations.
     *
     * @return a ready-to-use AuthenticationControllerApi instance
     */
    @Bean
    public AuthenticationControllerApi authenticationApi() {
        return new AuthenticationControllerApi(apiClient);
    }

    /**
     * Provides the {@link SessionControllerApi} bean for active session management and session closure.
     *
     * @return a ready-to-use SessionControllerApi instance
     */
    @Bean
    public SessionControllerApi sessionsApi() {
        return new SessionControllerApi(apiClient);
    }
}

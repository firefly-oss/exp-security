package com.firefly.experience.security.infra;

import org.springframework.stereotype.Component;

/**
 * Factory that creates and configures the SCA (Strong Customer Authentication) SDK client
 * and exposes domain API beans for dependency injection.
 *
 * <p><strong>Note:</strong> The {@code domain-common-sca-sdk} OpenAPI generation is currently
 * skipped ({@code <skip>true</skip>} in the SDK POM). To activate the {@link ScaOperationsApi}
 * bean below, run the SCA web module with the {@code generate-openapi} Maven profile, then
 * re-install the SDK:
 * <pre>{@code
 * cd domain-common-sca && mvn install -DskipTests
 * }</pre>
 * After the SDK contains compiled classes, uncomment the imports and bean method.
 */
@Component
public class ScaClientFactory {

    /**
     * No-arg constructor — no live beans to construct until the SCA SDK is generated.
     * When the SDK is available, inject {@link ScaProperties} and initialise the {@link com.firefly.domain.common.sca.sdk.invoker.ApiClient} here.
     */
    public ScaClientFactory() {
    }

    // TODO: Uncomment once domain-common-sca-sdk is generated (run with generate-openapi profile).
    //
    // import com.firefly.domain.common.sca.sdk.api.ScaOperationsApi;
    // import com.firefly.domain.common.sca.sdk.invoker.ApiClient;
    //
    // private final ApiClient apiClient;
    //
    // public ScaClientFactory(ScaProperties properties) {
    //     this.apiClient = new ApiClient();
    //     this.apiClient.setBasePath(properties.getBasePath());
    // }
    //
    // @Bean
    // public ScaOperationsApi scaOperationsApi() {
    //     return new ScaOperationsApi(apiClient);
    // }
}

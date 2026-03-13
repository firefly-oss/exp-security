package com.firefly.experience.security.infra;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for the Security Center domain-tier API.
 * <p>
 * Binds to {@code api-configuration.domain-platform.security-center} in application.yaml.
 */
@Component
@ConfigurationProperties(prefix = "api-configuration.domain-platform.security-center")
@Data
public class SecurityCenterProperties {

    /** Base URL of the Security Center service (e.g. {@code http://localhost:8030}). */
    private String basePath;
}

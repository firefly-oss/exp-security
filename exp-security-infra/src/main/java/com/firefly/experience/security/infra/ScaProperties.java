package com.firefly.experience.security.infra;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for the SCA (Strong Customer Authentication) domain-tier API.
 * <p>
 * Binds to {@code api-configuration.domain-platform.common-sca} in application.yaml.
 */
@Component
@ConfigurationProperties(prefix = "api-configuration.domain-platform.common-sca")
@Data
public class ScaProperties {

    /** Base URL of the SCA service (e.g. {@code http://localhost:8041}). */
    private String basePath;
}

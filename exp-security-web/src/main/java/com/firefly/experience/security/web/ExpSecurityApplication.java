package com.firefly.experience.security.web;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.web.reactive.config.EnableWebFlux;

/**
 * Spring Boot application entry point for the Experience Security service.
 * <p>
 * Provides REST APIs for security-related user journeys, composing domain-tier
 * services (e.g., SCA, security vault, user management) into channel-specific endpoints.
 * No own database — stateless composition of downstream domain SDKs.
 */
@SpringBootApplication(
        scanBasePackages = {
                "com.firefly.experience.security",
                "org.fireflyframework.web"
        }
)
@EnableWebFlux
@ConfigurationPropertiesScan
@OpenAPIDefinition(
        info = @Info(
                title = "${spring.application.name}",
                version = "${spring.application.version}",
                description = "${spring.application.description}",
                contact = @Contact(
                        name = "${spring.application.team.name}",
                        email = "${spring.application.team.email}"
                )
        ),
        servers = {
                @Server(
                        url = "http://core.getfirefly.io/exp-security",
                        description = "Development Environment"
                ),
                @Server(
                        url = "/",
                        description = "Local Development Environment"
                )
        }
)
public class ExpSecurityApplication {

    /**
     * Application entry point. Bootstraps the Spring context and starts the embedded server.
     *
     * @param args command-line arguments passed to the Spring application
     */
    public static void main(String[] args) {
        SpringApplication.run(ExpSecurityApplication.class, args);
    }
}

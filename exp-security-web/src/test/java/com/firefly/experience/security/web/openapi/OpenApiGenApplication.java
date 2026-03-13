package com.firefly.experience.security.web.openapi;

import org.fireflyframework.web.openapi.EnableOpenApiGen;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Lightweight Spring Boot application used exclusively during the Maven
 * {@code generate-openapi} profile to expose the OpenAPI spec on port 18080
 * so that {@code springdoc-openapi-maven-plugin} can fetch and write it
 * to {@code target/openapi/openapi.yml} for SDK generation.
 */
@EnableOpenApiGen
@ComponentScan(basePackages = "com.firefly.experience.security.web.controllers")
public class OpenApiGenApplication {
    public static void main(String[] args) {
        SpringApplication.run(OpenApiGenApplication.class, args);
    }
}

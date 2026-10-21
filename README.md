<a href="https://fireflysoftware.io/openplatform/">
<picture>
  <source media="(max-width: 640px) and (prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/firefly-oss/.github/main/profile/assets/banner-narrow-dark.svg">
  <source media="(max-width: 640px)" srcset="https://raw.githubusercontent.com/firefly-oss/.github/main/profile/assets/banner-narrow-light.svg">
  <source media="(prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/firefly-oss/.github/main/profile/assets/banner-dark.svg">
  <img src="https://raw.githubusercontent.com/firefly-oss/.github/main/profile/assets/banner-light.svg" alt="Firefly OpenPlatform - Experience - Security" width="100%">
</picture>
</a>

# Firefly OpenPlatform - Experience - Security

Developed by the Firefly Software Foundation. [dev@fireflysoftware.io](mailto:dev@fireflysoftware.io)

[Website](https://fireflysoftware.io/) · [OpenPlatform](https://fireflysoftware.io/openplatform/) · [Module catalogue](https://fireflysoftware.io/openplatform/modules/)

> Backend-for-Frontend service for authentication, Strong Customer Authentication (SCA), and session management

## Table of Contents
- [Overview](#overview)
- [Architecture](#architecture)
- [Module Structure](#module-structure)
- [Functional Verticals](#functional-verticals)
- [API Endpoints](#api-endpoints)
- [Domain SDK Dependencies](#domain-sdk-dependencies)
- [Configuration](#configuration)
- [Running Locally](#running-locally)
- [Testing](#testing)

## Overview

`exp-security` is the experience-layer service for the security operations the platform already exposes. It authenticates with username and password, refreshes and logs out sessions, resets a password through the security center, and generates or verifies an SCA challenge through the SCA service.

The service delegates authentication, password reset, and sessions to `domain-core-security-center`, and SCA challenge generation and verification to `domain-common-sca`. Endpoints with no backing operation are not part of this API. All endpoints are **stateless compositions** — no workflow state or Redis persistence is used.

MapStruct is used in the `-core` module to map between command DTOs and SDK request models. Each endpoint is atomic and independent, allowing the frontend to call them in any order without relying on shared session state within this service.

## Architecture

```
Frontend / Mobile App
         |
         v
exp-security  (port 8101)
         |
         +---> domain-core-security-center-sdk  (AuthenticationApi, SessionsApi)
         |
         +---> domain-common-sca-sdk             (ScaApi)
```

## Module Structure

| Module | Purpose |
|--------|---------|
| `exp-security-interfaces` | (Reserved for future shared contracts) |
| `exp-security-core` | Service interface, `SecurityServiceImpl`, MapStruct mapper, command DTOs, query DTOs (`AuthTokenDTO`, `SessionDTO`, `ScaChallengeDTO`, `ScaVerificationResultDTO`) |
| `exp-security-infra` | `SecurityCenterClientFactory` (AuthenticationApi, SessionsApi), `ScaClientFactory` (ScaApi), and their `@ConfigurationProperties` |
| `exp-security-web` | `SecurityController`, Spring Boot application class, `application.yaml` |
| `exp-security-sdk` | Auto-generated reactive SDK from the OpenAPI spec |

## Functional Verticals

| Vertical | Endpoints | Description |
|----------|-----------|-------------|
| Authentication | 3 | Password login, token refresh, logout |
| Password | 1 | Password reset through the security center |
| SCA | 2 | Generate a challenge for an existing operation, verify the response |
| Sessions | 2 | List active sessions, close a specific session |

## API Endpoints

### Authentication

| Method | Path | Description | Response Code |
|--------|------|-------------|---------------|
| `POST` | `/api/v1/experience/security/auth/login` | Authenticate with username/password; returns access and refresh tokens | `200 OK` |
| `POST` | `/api/v1/experience/security/auth/refresh` | Exchange a valid refresh token for a new access token | `200 OK` |
| `POST` | `/api/v1/experience/security/auth/logout` | Invalidate the current party session and IDP tokens | `204 No Content` |

### Password

| Method | Path | Description | Response Code |
|--------|------|-------------|---------------|
| `POST` | `/api/v1/experience/security/auth/password/reset` | Reset the password for the supplied account username | `202 Accepted` |

### Strong Customer Authentication (SCA)

| Method | Path | Description | Response Code |
|--------|------|-------------|---------------|
| `POST` | `/api/v1/experience/security/sca/challenge` | Generate a challenge for an existing SCA operation | `201 Created` |
| `POST` | `/api/v1/experience/security/sca/verify` | Verify the party's response to an issued SCA challenge | `200 OK` |

### Sessions

| Method | Path | Description | Response Code |
|--------|------|-------------|---------------|
| `GET` | `/api/v1/experience/security/sessions` | List active sessions for the authenticated party | `200 OK` |
| `DELETE` | `/api/v1/experience/security/sessions/{sessionId}` | Close a specific session by its identifier | `204 No Content` |

## Domain SDK Dependencies

| SDK | ClientFactory | APIs Used | Purpose |
|-----|--------------|-----------|---------|
| `domain-core-security-center-sdk` | `SecurityCenterClientFactory` | `AuthenticationControllerApi`, `SessionControllerApi` | Login, token refresh, logout, password reset, active sessions, session closure |
| `domain-common-sca-sdk` | `ScaClientFactory` | `ScaOperationsApi` | SCA challenge generation and verification |

## Configuration

```yaml
server:
  port: ${SERVER_PORT:8101}

api-configuration:
  domain-platform:
    security-center:
      base-path: ${SECURITY_CENTER_URL:http://localhost:8030}
    common-sca:
      base-path: ${SCA_URL:http://localhost:8041}
```

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | `8101` | HTTP server port |
| `SERVER_ADDRESS` | `localhost` | Bind address |
| `SECURITY_CENTER_URL` | `http://localhost:8030` | Base URL for `domain-core-security-center` |
| `SCA_URL` | `http://localhost:8041` | Base URL for `domain-common-sca` |

## Running Locally

```bash
# Prerequisites — ensure domain-core-security-center and domain-common-sca are running
cd exp-security
mvn spring-boot:run -pl exp-security-web
```

Server starts on port `8101`. Swagger UI: [http://localhost:8101/swagger-ui.html](http://localhost:8101/swagger-ui.html)

## Testing

```bash
mvn clean verify
```

Tests cover `SecurityServiceImpl` (unit tests with mocked SDK APIs using Mockito and `StepVerifier`) and `SecurityController` (WebTestClient-based controller tests verifying HTTP status codes, response body shapes, and error handling).

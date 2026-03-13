# exp-security

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

`exp-security` is the experience-layer service that provides all security-related capabilities consumed by digital frontends. It exposes atomic endpoints for user authentication (password-based and biometric), session lifecycle management, Strong Customer Authentication (SCA) challenge/verify flows, password reset and change, and security activity log queries.

The service acts as a security aggregator: it delegates to the `domain-core-security-center` service for authentication, session management, and activity log operations, and to `domain-common-sca` for SCA challenge issuance and verification. All endpoints are **stateless compositions** — no workflow state or Redis persistence is used.

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
| `exp-security-core` | Service interface, `SecurityServiceImpl`, MapStruct mapper, command DTOs, query DTOs (`AuthTokenDTO`, `SessionDTO`, `ScaChallengeDTO`, `ScaVerificationResultDTO`, `ActivityLogEntryDTO`) |
| `exp-security-infra` | `SecurityCenterClientFactory` (AuthenticationApi, SessionsApi), `ScaClientFactory` (ScaApi), and their `@ConfigurationProperties` |
| `exp-security-web` | `SecurityController`, Spring Boot application class, `application.yaml` |
| `exp-security-sdk` | Auto-generated reactive SDK from the OpenAPI spec |

## Functional Verticals

| Vertical | Endpoints | Description |
|----------|-----------|-------------|
| Authentication | 4 | Password login, biometric login, token refresh, logout |
| Password | 2 | Password reset (unauthenticated) and password change (authenticated) |
| SCA | 2 | Request an SCA challenge, verify the response |
| Sessions | 2 | List active sessions, close a specific session |
| Activity Log | 1 | Retrieve the security activity log in reverse chronological order |

## API Endpoints

### Authentication

| Method | Path | Description | Response Code |
|--------|------|-------------|---------------|
| `POST` | `/api/v1/experience/security/auth/login` | Authenticate with username/password; returns access and refresh tokens | `200 OK` |
| `POST` | `/api/v1/experience/security/auth/login/biometric` | Authenticate using a device-generated biometric token | `200 OK` |
| `POST` | `/api/v1/experience/security/auth/refresh` | Exchange a valid refresh token for a new access token | `200 OK` |
| `POST` | `/api/v1/experience/security/auth/logout` | Invalidate the current party session and IDP tokens | `204 No Content` |

### Password

| Method | Path | Description | Response Code |
|--------|------|-------------|---------------|
| `POST` | `/api/v1/experience/security/auth/password/reset` | Trigger a password-reset email for the given address | `202 Accepted` |
| `POST` | `/api/v1/experience/security/auth/password/change` | Change the authenticated party's password | `204 No Content` |

### Strong Customer Authentication (SCA)

| Method | Path | Description | Response Code |
|--------|------|-------------|---------------|
| `POST` | `/api/v1/experience/security/sca/challenge` | Issue an SCA challenge for the requested operation and mechanism | `201 Created` |
| `POST` | `/api/v1/experience/security/sca/verify` | Verify the party's response to an issued SCA challenge | `200 OK` |

### Sessions

| Method | Path | Description | Response Code |
|--------|------|-------------|---------------|
| `GET` | `/api/v1/experience/security/sessions` | List active sessions for the authenticated party | `200 OK` |
| `DELETE` | `/api/v1/experience/security/sessions/{sessionId}` | Close a specific session by its identifier | `204 No Content` |

### Activity Log

| Method | Path | Description | Response Code |
|--------|------|-------------|---------------|
| `GET` | `/api/v1/experience/security/activity-log` | Retrieve the security activity log in reverse chronological order | `200 OK` |

## Domain SDK Dependencies

| SDK | ClientFactory | APIs Used | Purpose |
|-----|--------------|-----------|---------|
| `domain-core-security-center-sdk` | `SecurityCenterClientFactory` | `AuthenticationApi`, `SessionsApi` | Login, token refresh, logout, active sessions, session closure |
| `domain-common-sca-sdk` | `ScaClientFactory` | `ScaApi` | SCA challenge issuance and verification |

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

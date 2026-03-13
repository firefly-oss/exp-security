package com.firefly.experience.security.core.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a single active session for the authenticated party.
 * Exposes device/network context and activity timestamps.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionDTO {

    /** Unique identifier for the session. */
    private UUID sessionId;

    /** User agent / device information associated with the session. */
    private String deviceInfo;

    /** IP address from which the session was initiated. */
    private String ipAddress;

    /** When the session was first created. */
    private LocalDateTime createdAt;

    /** Timestamp of the most recent activity within this session. */
    private LocalDateTime lastActivityAt;
}

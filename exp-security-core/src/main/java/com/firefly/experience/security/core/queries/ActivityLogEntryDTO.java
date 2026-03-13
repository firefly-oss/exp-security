package com.firefly.experience.security.core.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A single entry in the party's security activity log.
 * Captures what happened, when, from where, and whether it succeeded.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogEntryDTO {

    /** Unique log entry identifier. */
    private UUID id;

    /** Human-readable description of the action performed (e.g. {@code LOGIN}, {@code PASSWORD_CHANGE}). */
    private String action;

    /** Timestamp at which the action was performed. */
    private LocalDateTime timestamp;

    /** IP address from which the action originated. */
    private String ipAddress;

    /** Device or user-agent information captured at the time of the action. */
    private String deviceInfo;

    /** Outcome of the action (e.g. {@code SUCCESS}, {@code FAILURE}). */
    private String result;
}

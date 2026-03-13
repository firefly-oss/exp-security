package com.firefly.experience.security.core.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A Strong Customer Authentication (SCA) challenge issued to the party.
 * The party must complete this challenge before the associated operation is authorised.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScaChallengeDTO {

    /** Unique identifier for this specific challenge. */
    private UUID challengeId;

    /** Identifier of the operation this challenge is protecting. */
    private UUID operationId;

    /** Type of challenge issued (e.g. {@code OTP}, {@code BIOMETRIC}, {@code PUSH}). */
    private String challengeType;

    /** Timestamp after which this challenge is no longer valid. */
    private LocalDateTime expiresAt;
}

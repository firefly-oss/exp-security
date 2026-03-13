package com.firefly.experience.security.core.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Result returned after the party attempts to verify an SCA challenge.
 * Conveys whether verification succeeded and the exact moment it occurred.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScaVerificationResultDTO {

    /** Identifier of the operation that was being authorised. */
    private UUID operationId;

    /** Verification outcome (e.g. {@code VERIFIED}, {@code REJECTED}, {@code EXPIRED}). */
    private String status;

    /** Timestamp at which the verification was processed. */
    private LocalDateTime verifiedAt;
}

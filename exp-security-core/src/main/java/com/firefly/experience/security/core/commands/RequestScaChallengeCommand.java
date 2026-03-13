package com.firefly.experience.security.core.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command to request an SCA challenge for a specific sensitive operation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestScaChallengeCommand {

    /** Type of operation that requires SCA authorisation (e.g. {@code TRANSFER}, {@code CONTRACT_SIGN}). */
    private String operationType;

    /** Preferred challenge delivery mechanism (e.g. {@code OTP}, {@code PUSH}, {@code BIOMETRIC}). */
    private String challengeType;
}

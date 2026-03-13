package com.firefly.experience.security.core.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Command to submit the party's response to an issued SCA challenge.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyScaCommand {

    /** Identifier of the operation being authorised. */
    private UUID operationId;

    /** Identifier of the challenge being answered. */
    private UUID challengeId;

    /** One-time code or biometric assertion provided by the party. */
    private String verificationCode;
}

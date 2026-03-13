package com.firefly.experience.security.core.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command to trigger a password reset flow for a given email address.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetCommand {

    /** Email address associated with the account to reset. */
    private String email;
}

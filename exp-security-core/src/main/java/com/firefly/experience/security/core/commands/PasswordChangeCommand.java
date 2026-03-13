package com.firefly.experience.security.core.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command to change an authenticated party's password.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeCommand {

    /** The party's current password, required for verification before the change. */
    private String currentPassword;

    /** The desired new password. Never logged or persisted. */
    private String newPassword;
}

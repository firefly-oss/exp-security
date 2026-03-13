package com.firefly.experience.security.core.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command to authenticate a party using username/password credentials.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginCommand {

    /** Party's username or email used for authentication. */
    private String username;

    /** Party's password. Never logged or persisted. */
    private String password;

    /** Device information forwarded to the security center for session enrichment. */
    private String deviceInfo;
}

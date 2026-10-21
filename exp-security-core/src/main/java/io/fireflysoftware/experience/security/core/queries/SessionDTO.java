/*
 * Copyright 2025 Firefly Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.fireflysoftware.experience.security.core.queries;

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

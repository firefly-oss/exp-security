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

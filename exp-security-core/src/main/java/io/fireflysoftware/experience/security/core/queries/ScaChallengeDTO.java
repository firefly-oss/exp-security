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

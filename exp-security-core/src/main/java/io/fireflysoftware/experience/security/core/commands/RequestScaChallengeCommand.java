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


package io.fireflysoftware.experience.security.core.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Command to generate a challenge for an SCA operation that already exists.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestScaChallengeCommand {

    /** Identifier of the SCA operation the challenge belongs to. */
    private UUID operationId;

    /** Challenge mechanism the SCA service should generate ({@code OTP}, {@code PUSH}, or {@code BIOMETRIC}). */
    private String challengeType;
}

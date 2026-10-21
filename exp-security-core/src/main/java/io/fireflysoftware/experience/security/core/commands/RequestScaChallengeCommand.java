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

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

/**
 * Authentication token response returned after a successful login or token refresh.
 * Wraps the access token, refresh token, expiry, and token type issued by the IDP.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthTokenDTO {

    /** Bearer token used to authenticate subsequent API requests. */
    private String accessToken;

    /** Long-lived token used to obtain a new access token without re-authenticating. */
    private String refreshToken;

    /** Number of seconds until the access token expires. */
    private Long expiresIn;

    /** Token type (typically {@code Bearer}). */
    private String tokenType;
}

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


package io.fireflysoftware.experience.security.core.mappers;

import io.fireflysoftware.experience.security.core.queries.AuthTokenDTO;
import io.fireflysoftware.experience.security.core.queries.SessionDTO;
import io.fireflysoftware.security.center.sdk.model.AuthenticationResponse;
import io.fireflysoftware.security.center.sdk.model.SessionContextDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.UUID;

/**
 * MapStruct mapper that converts Security Center SDK model types to experience-layer DTOs.
 * Handles field-name mismatches and the {@code String → UUID} coercion required for session IDs.
 */
@Mapper(componentModel = "spring")
public interface SecurityMapper {

    /**
     * Maps an {@link AuthenticationResponse} from the Security Center SDK to an {@link AuthTokenDTO}.
     * All field names match ({@code accessToken}, {@code refreshToken}, {@code expiresIn}, {@code tokenType}),
     * so no explicit {@code @Mapping} annotations are required.
     *
     * @param source SDK authentication response
     * @return experience-layer token DTO
     */
    AuthTokenDTO toAuthTokenDTO(AuthenticationResponse source);

    /**
     * Maps a {@link SessionContextDTO} from the Security Center SDK to a {@link SessionDTO}.
     *
     * <p>Explicit mappings required:
     * <ul>
     *   <li>{@code sessionId} — String in SDK, UUID in DTO — uses {@code stringToUuid} converter</li>
     *   <li>{@code userAgent} → {@code deviceInfo} — field renamed in the experience layer</li>
     *   <li>{@code lastAccessedAt} → {@code lastActivityAt} — field renamed in the experience layer</li>
     * </ul>
     *
     * @param source SDK session context
     * @return experience-layer session DTO
     */
    @Mapping(target = "sessionId", source = "sessionId", qualifiedByName = "stringToUuid")
    @Mapping(target = "deviceInfo", source = "userAgent")
    @Mapping(target = "lastActivityAt", source = "lastAccessedAt")
    SessionDTO toSessionDTO(SessionContextDTO source);

    /**
     * Converts a nullable String to a {@link UUID}.
     *
     * @param value UUID string, may be {@code null}
     * @return parsed UUID, or {@code null} if input is {@code null}
     */
    @Named("stringToUuid")
    default UUID stringToUuid(String value) {
        return value != null ? UUID.fromString(value) : null;
    }
}

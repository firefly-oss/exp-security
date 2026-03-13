package com.firefly.experience.security.core.mappers;

import com.firefly.experience.security.core.queries.AuthTokenDTO;
import com.firefly.experience.security.core.queries.SessionDTO;
import com.firefly.security.center.sdk.model.AuthenticationResponse;
import com.firefly.security.center.sdk.model.SessionContextDTO;
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

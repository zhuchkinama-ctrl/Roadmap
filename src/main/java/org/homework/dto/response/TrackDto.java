package org.homework.dto.response;

import lombok.Data;
import java.time.Instant;

/**
 * DTO for Track entity.
 */
@Data
public class TrackDto {
    private Long id;
    private String title;
    private String description;
    private Long ownerId;
    private Instant createdAt;
    private Instant updatedAt;
}
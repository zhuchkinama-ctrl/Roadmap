package org.homework.dto.response;

import lombok.Data;
import java.time.Instant;

/**
 * DTO for Note entity.
 */
@Data
public class NoteDto {
    private Long id;
    private Long trackId;
    private Long parentId;
    private String title;
    private String content;
    private Integer orderIndex;
    private Instant createdAt;
    private Instant updatedAt;
}
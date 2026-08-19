package school.hei.students.model;

import java.util.UUID;
import lombok.Builder;

@Builder
public record Teacher(UUID id, UUID userId, String lastName, String firstName, boolean active) {}

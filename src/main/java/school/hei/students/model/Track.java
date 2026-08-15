package school.hei.students.model;

import java.util.UUID;
import lombok.Builder;

@Builder
public record Track(UUID id, String name) {}

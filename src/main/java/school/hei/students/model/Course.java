package school.hei.students.model;

import java.util.UUID;
import lombok.Builder;

@Builder
public record Course(
    UUID id, String code, String title, Integer credits, String level, String semester) {}

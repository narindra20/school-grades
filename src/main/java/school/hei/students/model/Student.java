package school.hei.students.model;

import java.util.UUID;
import lombok.Builder;

@Builder
public record Student(
    UUID id,
    UUID userId,
    UUID cohortId,
    String lastName,
    String firstName,
    String studentNumber,
    boolean workStudy,
    boolean active) {}

package school.hei.students.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record GradeHistory(
    UUID id, UUID gradeId, Double oldValue, Double newValue, String reason, Instant modifiedAt) {}

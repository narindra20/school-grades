package school.hei.students.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record Exam(
    UUID id,
    UUID assignmentId,
    String label,
    Instant examDate,
    Double coefficient,
    ExamType type) {}

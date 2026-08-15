package school.hei.students.model;

import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;

@Builder
public record Grade(UUID id, UUID studentId, UUID examId, Double value, LocalDate gradedDate) {}

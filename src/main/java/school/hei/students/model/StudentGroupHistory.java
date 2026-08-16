package school.hei.students.model;

import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;

@Builder
public record StudentGroupHistory(
    UUID id, UUID studentId, UUID groupId, LocalDate startDate, LocalDate endDate) {}

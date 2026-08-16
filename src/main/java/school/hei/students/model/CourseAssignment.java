package school.hei.students.model;

import java.util.UUID;
import lombok.Builder;

@Builder
public record CourseAssignment(UUID id, UUID courseId, String academicYear) {}

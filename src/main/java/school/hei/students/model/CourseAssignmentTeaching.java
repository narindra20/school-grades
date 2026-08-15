package school.hei.students.model;

import java.util.UUID;
import lombok.Builder;

@Builder
public record CourseAssignmentTeaching(UUID id, UUID assignmentId, UUID teacherId, UUID groupId) {}

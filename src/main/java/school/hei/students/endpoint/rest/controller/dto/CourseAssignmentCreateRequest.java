package school.hei.students.endpoint.rest.controller.dto;

import java.util.UUID;

public record CourseAssignmentCreateRequest(UUID courseId, String academicYear) {}

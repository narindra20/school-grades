package school.hei.students.endpoint.rest.controller.dto;

import java.util.UUID;

public record TeachingAssignRequest(UUID teacherId, UUID groupId) {}

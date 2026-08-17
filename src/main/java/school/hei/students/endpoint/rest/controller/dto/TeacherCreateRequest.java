package school.hei.students.endpoint.rest.controller.dto;

import java.util.UUID;

public record TeacherCreateRequest(UUID userId, String lastName, String firstName) {}

package school.hei.students.endpoint.rest.controller.dto;

import java.util.UUID;

public record StudentCreateRequest(
    UUID userId,
    UUID cohortId,
    String lastName,
    String firstName,
    String studentNumber,
    boolean workStudy) {}

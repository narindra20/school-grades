package school.hei.students.endpoint.rest.controller.dto;

import java.util.UUID;

public record StudentUpdateRequest(
    String lastName, String firstName, Boolean workStudy, UUID cohortId) {}

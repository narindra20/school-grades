package school.hei.students.endpoint.rest.controller.dto;

import java.util.UUID;

public record GroupCreateRequest(String code, UUID trackId, UUID cohortId) {}

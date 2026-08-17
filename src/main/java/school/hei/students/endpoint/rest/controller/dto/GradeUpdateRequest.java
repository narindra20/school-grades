package school.hei.students.endpoint.rest.controller.dto;

import lombok.Builder;

@Builder
public record GradeUpdateRequest(Double value, String reason) {}

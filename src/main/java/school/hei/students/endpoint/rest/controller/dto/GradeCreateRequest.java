package school.hei.students.endpoint.rest.controller.dto;

import java.util.UUID;
import lombok.Builder;

@Builder
public record GradeCreateRequest(UUID studentId, UUID examId, Double value) {}

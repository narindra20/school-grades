package school.hei.students.endpoint.rest.controller.dto;

import java.time.Instant;
import lombok.Builder;

@Builder
public record ExamUpdateRequest(String label, Instant examDate, Double coefficient) {}

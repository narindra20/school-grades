package school.hei.students.endpoint.rest.controller.dto;

import java.time.Instant;
import lombok.Builder;
import school.hei.students.model.ExamType;

@Builder
public record ExamCreateRequest(
    String label, Instant examDate, Double coefficient, ExamType type) {}

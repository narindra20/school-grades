package school.hei.students.endpoint.rest.controller.dto;

import java.time.LocalDate;
import java.util.UUID;

public record ChangeGroupRequest(UUID newGroupId, LocalDate changeDate) {}

package school.hei.students.model;

import java.util.UUID;
import lombok.Builder;

@Builder
public record Group(UUID id, String code, UUID trackId, UUID cohortId) {}

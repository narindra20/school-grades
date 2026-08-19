package school.hei.students.model;

import java.util.UUID;
import lombok.Builder;

@Builder
public record User(UUID id, String email, String password, Role role, boolean active) {}

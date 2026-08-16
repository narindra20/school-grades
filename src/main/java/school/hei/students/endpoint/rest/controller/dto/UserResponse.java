package school.hei.students.endpoint.rest.controller.dto;

import java.util.UUID;
import school.hei.students.model.Role;
import school.hei.students.model.User;

public record UserResponse(UUID id, String email, Role role, boolean active) {
  public static UserResponse from(User user) {
    return new UserResponse(user.id(), user.email(), user.role(), user.active());
  }
}

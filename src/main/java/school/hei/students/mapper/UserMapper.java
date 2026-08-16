package school.hei.students.mapper;

import java.util.List;
import org.springframework.stereotype.Component;
import school.hei.students.model.Role;
import school.hei.students.model.User;
import school.hei.students.repository.model.JUser;

@Component
public class UserMapper {
  public User toModel(JUser entity) {
    return User.builder()
        .id(entity.getId())
        .email(entity.getEmail())
        .password(entity.getPassword())
        .role(Role.valueOf(entity.getRole()))
        .active(entity.isActive())
        .build();
  }

  public List<User> toModel(List<JUser> entities) {
    return entities.stream().map(this::toModel).toList();
  }

  public JUser toEntity(User model) {
    return JUser.builder()
        .id(model.id())
        .email(model.email())
        .password(model.password())
        .role(model.role().name())
        .active(model.active())
        .build();
  }
}

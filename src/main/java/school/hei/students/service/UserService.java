package school.hei.students.service;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import school.hei.students.mapper.UserMapper;
import school.hei.students.model.Role;
import school.hei.students.model.User;
import school.hei.students.repository.UserRepository;

@Service
@AllArgsConstructor
public class UserService {
  private final UserRepository repository;
  private final UserMapper mapper;
  private final PasswordEncoder passwordEncoder;

  public User create(UUID id, String email, String password, Role role) {
    var toSave =
        User.builder()
            .id(id)
            .email(email)
            .password(passwordEncoder.encode(password))
            .role(role)
            .active(true)
            .build();
    return mapper.toModel(repository.save(mapper.toEntity(toSave)));
  }

  public User getById(UUID id) {
    return mapper.toModel(repository.findById(id).orElseThrow(() -> new UserNotFoundException(id)));
  }

  public User getByEmail(String email) {
    return mapper.toModel(
        repository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email)));
  }

  public List<User> getAll() {
    return mapper.toModel(repository.findAll());
  }

  public User update(UUID id, String email, String password, Role role) {
    var existing =
        mapper.toModel(repository.findById(id).orElseThrow(() -> new UserNotFoundException(id)));
    var updated =
        User.builder()
            .id(existing.id())
            .email(email != null ? email : existing.email())
            .password(password != null ? passwordEncoder.encode(password) : existing.password())
            .role(role != null ? role : existing.role())
            .active(existing.active())
            .build();
    return mapper.toModel(repository.save(mapper.toEntity(updated)));
  }

  public void deactivate(UUID id) {
    var existing = repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    existing.setActive(false);
    repository.save(existing);
  }

  public boolean matchesPassword(String rawPassword, String encodedPassword) {
    return passwordEncoder.matches(rawPassword, encodedPassword);
  }
}

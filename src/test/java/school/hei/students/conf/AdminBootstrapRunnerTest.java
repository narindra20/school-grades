package school.hei.students.conf;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import school.hei.students.model.Role;
import school.hei.students.repository.UserRepository;
import school.hei.students.repository.model.JUser;
import school.hei.students.service.UserService;

@ExtendWith(MockitoExtension.class)
class AdminBootstrapRunnerTest {
  @Mock private UserRepository userRepository;
  @Mock private UserService userService;

  @Test
  void creates_admin_when_none_exists_and_credentials_configured() {
    when(userRepository.findAll()).thenReturn(List.of());
    var runner =
        new AdminBootstrapRunner(
            userRepository, userService, "admin@hei.school", "test-admin-password-only");
    runner.run(new DefaultApplicationArguments());
    verify(userService, times(1))
        .create(any(), eq("admin@hei.school"), eq("test-admin-password-only"), eq(Role.ADMIN));
  }

  @Test
  void skips_when_admin_already_exists() {
    var existingAdmin = new JUser();
    existingAdmin.setRole(Role.ADMIN.name());
    when(userRepository.findAll()).thenReturn(List.of(existingAdmin));
    var runner =
        new AdminBootstrapRunner(
            userRepository, userService, "admin@hei.school", "test-admin-password-only");
    runner.run(new DefaultApplicationArguments());
    verify(userService, never()).create(any(), any(), any(), any());
  }

  @Test
  void skips_when_no_admin_and_credentials_not_configured() {
    when(userRepository.findAll()).thenReturn(List.of());
    var runner = new AdminBootstrapRunner(userRepository, userService, "", "");
    runner.run(new DefaultApplicationArguments());
    verify(userService, never()).create(any(), any(), any(), any());
  }
}

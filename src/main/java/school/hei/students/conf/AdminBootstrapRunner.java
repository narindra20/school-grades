package school.hei.students.conf;

import static java.util.UUID.randomUUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import school.hei.students.model.Role;
import school.hei.students.repository.UserRepository;
import school.hei.students.service.UserService;

@Component
@Slf4j
public class AdminBootstrapRunner implements ApplicationRunner {
  private final UserRepository userRepository;
  private final UserService userService;
  private final String bootstrapEmail;
  private final String bootstrapPassword;

  public AdminBootstrapRunner(
      UserRepository userRepository,
      UserService userService,
      @Value("${admin.bootstrap.email:}") String bootstrapEmail,
      @Value("${admin.bootstrap.password:}") String bootstrapPassword) {
    this.userRepository = userRepository;
    this.userService = userService;
    this.bootstrapEmail = bootstrapEmail;
    this.bootstrapPassword = bootstrapPassword;
  }

  @Override
  public void run(ApplicationArguments args) {
    var hasAdmin = userRepository.findAll().stream().anyMatch(u -> "ADMIN".equals(u.getRole()));
    if (hasAdmin) {
      return;
    }
    if (bootstrapEmail.isBlank() || bootstrapPassword.isBlank()) {
      log.warn(
          "No admin exists and ADMIN_BOOTSTRAP_EMAIL/ADMIN_BOOTSTRAP_PASSWORD are not set, skipping"
              + " bootstrap");
      return;
    }
    userService.create(randomUUID(), bootstrapEmail, bootstrapPassword, Role.ADMIN);
    log.warn("Bootstrap admin created: {}", bootstrapEmail);
  }
}

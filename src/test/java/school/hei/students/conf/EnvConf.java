package school.hei.students.conf;

import org.springframework.test.context.DynamicPropertyRegistry;

public class EnvConf {
  public void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("jwt.secret", () -> "integration-test-jwt-key-not-a-real-secret");
  }
}

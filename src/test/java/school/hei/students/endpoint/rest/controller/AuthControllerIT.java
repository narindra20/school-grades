package school.hei.students.endpoint.rest.controller;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import school.hei.students.conf.FacadeIT;
import school.hei.students.endpoint.rest.controller.dto.LoginRequest;
import school.hei.students.endpoint.rest.controller.dto.LoginResponse;
import school.hei.students.model.Role;
import school.hei.students.service.UserService;

class AuthControllerIT extends FacadeIT {
  @Autowired private TestRestTemplate restTemplate;
  @Autowired private UserService userService;

  @Test
  void login_ok() {
    var email = "admin-" + randomUUID() + "@hei.school";
    userService.create(randomUUID(), email, "test-password-only", Role.ADMIN);

    var response =
        restTemplate.postForEntity(
            "/auth/login", new LoginRequest(email, "test-password-only"), LoginResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().token()).isNotBlank();
    assertThat(response.getBody().role()).isEqualTo(Role.ADMIN);
  }

  @Test
  void login_wrong_password_ko() {
    var email = "student-" + randomUUID() + "@hei.school";
    userService.create(randomUUID(), email, "test-password-only", Role.STUDENT);

    var response =
        restTemplate.postForEntity(
            "/auth/login", new LoginRequest(email, "wrong-test-password"), LoginResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void login_unknown_email_ko() {
    var email = "unknown-" + randomUUID() + "@hei.school";

    var response =
        restTemplate.postForEntity(
            "/auth/login", new LoginRequest(email, "test-password-only"), LoginResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }
}

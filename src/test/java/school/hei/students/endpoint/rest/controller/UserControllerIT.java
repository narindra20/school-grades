package school.hei.students.endpoint.rest.controller;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PUT;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import school.hei.students.conf.FacadeIT;
import school.hei.students.endpoint.rest.controller.dto.LoginRequest;
import school.hei.students.endpoint.rest.controller.dto.LoginResponse;
import school.hei.students.endpoint.rest.controller.dto.UserCreateRequest;
import school.hei.students.endpoint.rest.controller.dto.UserResponse;
import school.hei.students.endpoint.rest.controller.dto.UserUpdateRequest;
import school.hei.students.model.Role;
import school.hei.students.service.UserService;

class UserControllerIT extends FacadeIT {
  @Autowired private TestRestTemplate restTemplate;
  @Autowired private UserService userService;

  private String loginAsAdmin() {
    var email = "admin-" + randomUUID() + "@hei.school";
    userService.create(randomUUID(), email, "test-password-only", Role.ADMIN);
    var response =
        restTemplate.postForEntity(
            "/auth/login", new LoginRequest(email, "test-password-only"), LoginResponse.class);
    return response.getBody().token();
  }

  private HttpEntity<Void> authHeader(String token) {
    var headers = new HttpHeaders();
    headers.setBearerAuth(token);
    return new HttpEntity<>(headers);
  }

  @Test
  void create_and_get_user() {
    var token = loginAsAdmin();
    var teacherEmail = "teacher-" + randomUUID() + "@hei.school";
    var createRequest = new UserCreateRequest(teacherEmail, "test-password-only", Role.TEACHER);
    var createHeaders = new HttpHeaders();
    createHeaders.setBearerAuth(token);
    var createEntity = new HttpEntity<>(createRequest, createHeaders);
    var createResponse =
        restTemplate.postForEntity("/admin/users", createEntity, UserResponse.class);
    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(createResponse.getBody().email()).isEqualTo(teacherEmail);
    assertThat(createResponse.getBody().active()).isTrue();
    var getResponse =
        restTemplate.exchange(
            "/admin/users/" + createResponse.getBody().id(),
            GET,
            authHeader(token),
            UserResponse.class);
    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(getResponse.getBody().role()).isEqualTo(Role.TEACHER);
  }

  @Test
  void update_user_ok() {
    var token = loginAsAdmin();
    var email = "toupdate-" + randomUUID() + "@hei.school";
    var created = userService.create(randomUUID(), email, "test-password-only", Role.STUDENT);
    var updateRequest = new UserUpdateRequest(null, null, Role.TEACHER);
    var updateHeaders = new HttpHeaders();
    updateHeaders.setBearerAuth(token);
    var updateEntity = new HttpEntity<>(updateRequest, updateHeaders);
    var updateResponse =
        restTemplate.exchange(
            "/admin/users/" + created.id(), PUT, updateEntity, UserResponse.class);
    assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(updateResponse.getBody().role()).isEqualTo(Role.TEACHER);
  }

  @Test
  void deactivate_user_soft_delete() {
    var token = loginAsAdmin();
    var email = "todeactivate-" + randomUUID() + "@hei.school";
    var created = userService.create(randomUUID(), email, "pass", Role.STUDENT);
    var deleteResponse =
        restTemplate.exchange(
            "/admin/users/" + created.id(), DELETE, authHeader(token), Void.class);
    assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    var fetched = userService.getById(created.id());
    assertThat(fetched.active()).isFalse();
  }

  @Test
  void access_denied_without_admin_role() {
    var email = "plainstudent-" + randomUUID() + "@hei.school";
    userService.create(randomUUID(), email, "test-password-only", Role.STUDENT);
    var loginResponse =
        restTemplate.postForEntity(
            "/auth/login", new LoginRequest(email, "test-password-only"), LoginResponse.class);
    var studentToken = loginResponse.getBody().token();
    var response =
        restTemplate.exchange("/admin/users", GET, authHeader(studentToken), String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void access_denied_without_token() {
    var response = restTemplate.getForEntity("/admin/users", String.class);
    assertThat(response.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
  }
}

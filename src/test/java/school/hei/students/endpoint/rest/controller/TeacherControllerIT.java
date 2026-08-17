package school.hei.students.endpoint.rest.controller;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import school.hei.students.conf.FacadeIT;
import school.hei.students.endpoint.rest.controller.dto.LoginRequest;
import school.hei.students.endpoint.rest.controller.dto.LoginResponse;
import school.hei.students.endpoint.rest.controller.dto.TeacherCreateRequest;
import school.hei.students.model.Role;
import school.hei.students.model.Teacher;
import school.hei.students.service.UserService;

class TeacherControllerIT extends FacadeIT {
  @Autowired private TestRestTemplate restTemplate;
  @Autowired private UserService userService;

  private String loginAsAdmin() {
    var email = "admin-" + randomUUID() + "@hei.school";
    userService.create(randomUUID(), email, "password123", Role.ADMIN);
    var response =
        restTemplate.postForEntity(
            "/auth/login", new LoginRequest(email, "password123"), LoginResponse.class);
    return response.getBody().token();
  }

  private HttpHeaders authHeaders(String token) {
    var headers = new HttpHeaders();
    headers.setBearerAuth(token);
    return headers;
  }

  @Test
  void create_and_deactivate_teacher() {
    var token = loginAsAdmin();
    var teacherUserId =
        userService
            .create(randomUUID(), "teacher-" + randomUUID() + "@hei.school", "pass", Role.TEACHER)
            .id();
    var createEntity =
        new HttpEntity<>(
            new TeacherCreateRequest(teacherUserId, "Rakoto", "Jean"), authHeaders(token));
    var teacher =
        restTemplate.postForEntity("/admin/teachers", createEntity, Teacher.class).getBody();
    assertThat(teacher.active()).isTrue();
    var deleteEntity = new HttpEntity<Void>(authHeaders(token));
    var deleteResponse =
        restTemplate.exchange("/admin/teachers/" + teacher.id(), DELETE, deleteEntity, Void.class);
    assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    var getResponse =
        restTemplate.exchange("/admin/teachers/" + teacher.id(), GET, deleteEntity, Teacher.class);
    assertThat(getResponse.getBody().active()).isFalse();
  }
}

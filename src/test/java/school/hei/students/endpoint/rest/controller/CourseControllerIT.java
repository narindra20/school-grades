package school.hei.students.endpoint.rest.controller;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.DELETE;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import school.hei.students.conf.FacadeIT;
import school.hei.students.endpoint.rest.controller.dto.CourseAssignmentCreateRequest;
import school.hei.students.endpoint.rest.controller.dto.CourseCreateRequest;
import school.hei.students.endpoint.rest.controller.dto.LoginRequest;
import school.hei.students.endpoint.rest.controller.dto.LoginResponse;
import school.hei.students.model.Course;
import school.hei.students.model.CourseAssignment;
import school.hei.students.model.Role;
import school.hei.students.service.UserService;

class CourseControllerIT extends FacadeIT {
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
  void create_course_ok() {
    var token = loginAsAdmin();
    var createEntity =
        new HttpEntity<>(
            new CourseCreateRequest("code-" + randomUUID(), "Prog2", 5, "L2", "S1"),
            authHeaders(token));
    var response = restTemplate.postForEntity("/admin/courses", createEntity, Course.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
  }

  @Test
  void delete_course_with_assignment_ko() {
    var token = loginAsAdmin();
    var courseEntity =
        new HttpEntity<>(
            new CourseCreateRequest("code-" + randomUUID(), "Prog3", 5, "L3", "S1"),
            authHeaders(token));
    var course = restTemplate.postForEntity("/admin/courses", courseEntity, Course.class).getBody();
    var assignmentEntity =
        new HttpEntity<>(
            new CourseAssignmentCreateRequest(course.id(), "2025-2026"), authHeaders(token));
    restTemplate.postForEntity(
        "/admin/course-assignments", assignmentEntity, CourseAssignment.class);
    var deleteEntity = new HttpEntity<Void>(authHeaders(token));
    var deleteResponse =
        restTemplate.exchange("/admin/courses/" + course.id(), DELETE, deleteEntity, String.class);
    assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }
}

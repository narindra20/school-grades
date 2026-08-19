package school.hei.students.endpoint.rest.controller;

import static java.time.LocalDate.now;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import school.hei.students.conf.FacadeIT;
import school.hei.students.endpoint.rest.controller.dto.ChangeGroupRequest;
import school.hei.students.endpoint.rest.controller.dto.CohortCreateRequest;
import school.hei.students.endpoint.rest.controller.dto.GroupCreateRequest;
import school.hei.students.endpoint.rest.controller.dto.LoginRequest;
import school.hei.students.endpoint.rest.controller.dto.LoginResponse;
import school.hei.students.endpoint.rest.controller.dto.StudentCreateRequest;
import school.hei.students.model.Cohort;
import school.hei.students.model.Group;
import school.hei.students.model.Role;
import school.hei.students.model.Student;
import school.hei.students.model.StudentGroupHistory;
import school.hei.students.service.UserService;

class StudentControllerIT extends FacadeIT {
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
  void create_student_change_group_and_get_history() {
    var token = loginAsAdmin();
    var studentUserId =
        userService
            .create(randomUUID(), "student-" + randomUUID() + "@hei.school", "pass", Role.STUDENT)
            .id();
    var cohortEntity = new HttpEntity<>(new CohortCreateRequest(2025), authHeaders(token));
    var cohort = restTemplate.postForEntity("/admin/cohorts", cohortEntity, Cohort.class).getBody();
    var studentEntity =
        new HttpEntity<>(
            new StudentCreateRequest(
                studentUserId,
                cohort.id(),
                "Rasoa",
                "Marie",
                "24" + randomUUID().toString().substring(0, 3),
                false),
            authHeaders(token));
    var student =
        restTemplate.postForEntity("/admin/students", studentEntity, Student.class).getBody();
    var groupEntity =
        new HttpEntity<>(new GroupCreateRequest("K1", null, cohort.id()), authHeaders(token));
    var group = restTemplate.postForEntity("/admin/groups", groupEntity, Group.class).getBody();
    var changeEntity =
        new HttpEntity<>(new ChangeGroupRequest(group.id(), now()), authHeaders(token));
    var changeResponse =
        restTemplate.postForEntity(
            "/admin/students/" + student.id() + "/change-group",
            changeEntity,
            StudentGroupHistory.class);
    assertThat(changeResponse.getBody().groupId()).isEqualTo(group.id());
    var historyEntity = new HttpEntity<Void>(authHeaders(token));
    var historyResponse =
        restTemplate.exchange(
            "/admin/students/" + student.id() + "/group-history",
            GET,
            historyEntity,
            StudentGroupHistory[].class);
    assertThat(historyResponse.getBody()).hasSize(1);
  }
}

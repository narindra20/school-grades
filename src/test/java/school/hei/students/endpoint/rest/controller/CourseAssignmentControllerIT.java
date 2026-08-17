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
import school.hei.students.endpoint.rest.controller.dto.CohortCreateRequest;
import school.hei.students.endpoint.rest.controller.dto.CourseAssignmentCreateRequest;
import school.hei.students.endpoint.rest.controller.dto.CourseCreateRequest;
import school.hei.students.endpoint.rest.controller.dto.GroupCreateRequest;
import school.hei.students.endpoint.rest.controller.dto.LoginRequest;
import school.hei.students.endpoint.rest.controller.dto.LoginResponse;
import school.hei.students.endpoint.rest.controller.dto.TeacherCreateRequest;
import school.hei.students.endpoint.rest.controller.dto.TeachingAssignRequest;
import school.hei.students.model.Cohort;
import school.hei.students.model.Course;
import school.hei.students.model.CourseAssignment;
import school.hei.students.model.CourseAssignmentTeaching;
import school.hei.students.model.Group;
import school.hei.students.model.Role;
import school.hei.students.model.Teacher;
import school.hei.students.service.UserService;

class CourseAssignmentControllerIT extends FacadeIT {
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
  void create_assignment_and_assign_teaching() {
    var token = loginAsAdmin();
    var courseEntity =
        new HttpEntity<>(
            new CourseCreateRequest("code-" + randomUUID(), "Bdd", 5, "L2", "S1"),
            authHeaders(token));
    var course = restTemplate.postForEntity("/admin/courses", courseEntity, Course.class).getBody();
    var assignmentEntity =
        new HttpEntity<>(
            new CourseAssignmentCreateRequest(course.id(), "2025-2026"), authHeaders(token));
    var assignment =
        restTemplate
            .postForEntity("/admin/course-assignments", assignmentEntity, CourseAssignment.class)
            .getBody();
    var teacherUserId =
        userService
            .create(randomUUID(), "teacher-" + randomUUID() + "@hei.school", "pass", Role.TEACHER)
            .id();
    var teacherEntity =
        new HttpEntity<>(
            new TeacherCreateRequest(teacherUserId, "Rabe", "Paul"), authHeaders(token));
    var teacher =
        restTemplate.postForEntity("/admin/teachers", teacherEntity, Teacher.class).getBody();
    var cohortEntity = new HttpEntity<>(new CohortCreateRequest(2025), authHeaders(token));
    var cohort = restTemplate.postForEntity("/admin/cohorts", cohortEntity, Cohort.class).getBody();
    var groupEntity =
        new HttpEntity<>(new GroupCreateRequest("K1", null, cohort.id()), authHeaders(token));
    var group = restTemplate.postForEntity("/admin/groups", groupEntity, Group.class).getBody();
    var teachingEntity =
        new HttpEntity<>(new TeachingAssignRequest(teacher.id(), group.id()), authHeaders(token));
    var teachingResponse =
        restTemplate.postForEntity(
            "/admin/course-assignments/" + assignment.id() + "/teaching",
            teachingEntity,
            CourseAssignmentTeaching.class);
    assertThat(teachingResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    var removeEntity = new HttpEntity<Void>(authHeaders(token));
    var removeResponse =
        restTemplate.exchange(
            "/admin/course-assignment-teaching/" + teachingResponse.getBody().id(),
            DELETE,
            removeEntity,
            Void.class);
    assertThat(removeResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
  }
}

package school.hei.students.endpoint.rest.controller;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import school.hei.students.conf.FacadeIT;
import school.hei.students.endpoint.rest.controller.dto.ExamCreateRequest;
import school.hei.students.endpoint.rest.controller.dto.ExamUpdateRequest;
import school.hei.students.endpoint.rest.controller.dto.GradeCreateRequest;
import school.hei.students.endpoint.rest.controller.dto.LoginRequest;
import school.hei.students.endpoint.rest.controller.dto.LoginResponse;
import school.hei.students.model.Exam;
import school.hei.students.model.ExamType;
import school.hei.students.model.Grade;
import school.hei.students.model.Role;
import school.hei.students.repository.StudentRepository;
import school.hei.students.repository.model.JStudent;
import school.hei.students.service.UserService;

class ExamControllerIT extends FacadeIT {
  private static final AtomicLong STUDENT_NUMBER_SEQ = new AtomicLong();

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private UserService userService;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private StudentRepository studentRepository;

  private String login(String email, String password) {
    var response =
        restTemplate.postForEntity(
            "/auth/login", new LoginRequest(email, password), LoginResponse.class);
    return response.getBody().token();
  }

  private HttpHeaders headers(String token) {
    var headers = new HttpHeaders();
    headers.setBearerAuth(token);
    return headers;
  }

  private String adminToken() {
    var email = "admin-" + randomUUID() + "@hei.school";
    userService.create(randomUUID(), email, "test-password-only", Role.ADMIN);
    return login(email, "test-password-only");
  }

  private String studentToken() {
    var email = "student-" + randomUUID() + "@hei.school";
    userService.create(randomUUID(), email, "test-password-only", Role.STUDENT);
    return login(email, "test-password-only");
  }

  private UUID insertCourseAssignment() {
    var courseId = randomUUID();
    jdbcTemplate.update(
        "INSERT INTO course (id, code, title, credits, level, semester) VALUES (?, ?, ?, ?, ?, ?)",
        courseId,
        "prog3-" + courseId,
        "Programming 3",
        6,
        "L2",
        "S3");
    var assignmentId = randomUUID();
    jdbcTemplate.update(
        "INSERT INTO course_assignment (id, course_id, academic_year) VALUES (?, ?, ?)",
        assignmentId,
        courseId,
        "2025-2026");
    return assignmentId;
  }

  private UUID insertCohort() {
    var cohortId = randomUUID();
    jdbcTemplate.update("INSERT INTO cohort (id, entry_year) VALUES (?, ?)", cohortId, 2025);
    return cohortId;
  }

  private JStudent createStudent(UUID cohortId) {
    var email = "student-" + randomUUID() + "@hei.school";
    var user = userService.create(randomUUID(), email, "test-password-only", Role.STUDENT);
    return studentRepository.save(
        JStudent.builder()
            .userId(user.id())
            .cohortId(cohortId)
            .lastName("Rakoto")
            .firstName("Fitia")
            .studentNumber("25" + STUDENT_NUMBER_SEQ.incrementAndGet())
            .workStudy(false)
            .active(true)
            .build());
  }

  @Test
  void admin_can_create_list_get_and_update_exam() {
    var assignmentId = insertCourseAssignment();
    var token = adminToken();

    var createRequest =
        ExamCreateRequest.builder()
            .label("Theorie1")
            .examDate(Instant.now())
            .coefficient(0.5)
            .build();
    var createResponse =
        restTemplate.exchange(
            "/admin/course-assignments/" + assignmentId + "/exams",
            POST,
            new HttpEntity<>(createRequest, headers(token)),
            Exam.class);
    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(createResponse.getBody().type()).isEqualTo(ExamType.REGULAR);
    var examId = createResponse.getBody().id();

    var listResponse =
        restTemplate.exchange(
            "/admin/course-assignments/" + assignmentId + "/exams",
            GET,
            new HttpEntity<>(headers(token)),
            new ParameterizedTypeReference<List<Exam>>() {});
    assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(listResponse.getBody()).hasSize(1);

    var getResponse =
        restTemplate.exchange(
            "/admin/exams/" + examId, GET, new HttpEntity<>(headers(token)), Exam.class);
    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(getResponse.getBody().label()).isEqualTo("Theorie1");

    var updateRequest =
        ExamUpdateRequest.builder()
            .label("Theorie1-bis")
            .examDate(Instant.now())
            .coefficient(0.7)
            .build();
    var updateResponse =
        restTemplate.exchange(
            "/admin/exams/" + examId,
            PUT,
            new HttpEntity<>(updateRequest, headers(token)),
            Exam.class);
    assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(updateResponse.getBody().label()).isEqualTo("Theorie1-bis");
    assertThat(updateResponse.getBody().coefficient()).isEqualTo(0.7);
  }

  @Test
  void non_admin_cannot_create_exam() {
    var assignmentId = insertCourseAssignment();
    var token = studentToken();

    var createRequest =
        ExamCreateRequest.builder()
            .label("Theorie2")
            .examDate(Instant.now())
            .coefficient(0.5)
            .build();
    var response =
        restTemplate.exchange(
            "/admin/course-assignments/" + assignmentId + "/exams",
            POST,
            new HttpEntity<>(createRequest, headers(token)),
            String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void create_exam_rejects_non_positive_coefficient() {
    var assignmentId = insertCourseAssignment();
    var token = adminToken();

    var createRequest =
        ExamCreateRequest.builder().label("Bad").examDate(Instant.now()).coefficient(0.0).build();
    var response =
        restTemplate.exchange(
            "/admin/course-assignments/" + assignmentId + "/exams",
            POST,
            new HttpEntity<>(createRequest, headers(token)),
            String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void delete_exam_conflict_when_grade_linked() {
    var assignmentId = insertCourseAssignment();
    var token = adminToken();
    var createRequest =
        ExamCreateRequest.builder().label("API").examDate(Instant.now()).coefficient(0.4).build();
    var exam =
        restTemplate
            .exchange(
                "/admin/course-assignments/" + assignmentId + "/exams",
                POST,
                new HttpEntity<>(createRequest, headers(token)),
                Exam.class)
            .getBody();
    var cohortId = insertCohort();
    var student = createStudent(cohortId);
    var gradeRequest =
        GradeCreateRequest.builder()
            .studentId(student.getId())
            .examId(exam.id())
            .value(13.0)
            .build();
    restTemplate.exchange(
        "/students/" + student.getId() + "/grades",
        POST,
        new HttpEntity<>(gradeRequest, headers(token)),
        Grade.class);

    var deleteResponse =
        restTemplate.exchange(
            "/admin/exams/" + exam.id(), DELETE, new HttpEntity<>(headers(token)), String.class);
    assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void delete_exam_ok_when_no_grade_linked() {
    var assignmentId = insertCourseAssignment();
    var token = adminToken();
    var createRequest =
        ExamCreateRequest.builder()
            .label("ToDelete")
            .examDate(Instant.now())
            .coefficient(0.4)
            .build();
    var exam =
        restTemplate
            .exchange(
                "/admin/course-assignments/" + assignmentId + "/exams",
                POST,
                new HttpEntity<>(createRequest, headers(token)),
                Exam.class)
            .getBody();

    var deleteResponse =
        restTemplate.exchange(
            "/admin/exams/" + exam.id(), DELETE, new HttpEntity<>(headers(token)), Void.class);
    assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    var getResponse =
        restTemplate.exchange(
            "/admin/exams/" + exam.id(), GET, new HttpEntity<>(headers(token)), String.class);
    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void access_denied_without_token() {
    var response =
        restTemplate.exchange(
            "/admin/exams/" + randomUUID(), GET, new HttpEntity<>(new HttpHeaders()), String.class);
    assertThat(response.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
  }
}
